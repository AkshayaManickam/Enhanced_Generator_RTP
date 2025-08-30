package com.finzly.galaxy.rtp.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finzly.galaxy.rtp.model.Pacs008Document;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

public class RTPRuleBookValidator {

    private JsonNode rulebook;
    private Map<String, String> fieldAliases;

    public RTPRuleBookValidator(String rulebookPath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        rulebook = mapper.readTree(new File(rulebookPath));

        // Map rulebook names to actual Java fields
        fieldAliases = Map.of(
                "InstdAmt", "Amt"
        );
    }

    public List<String> validate(Pacs008Document doc) throws IllegalAccessException {
        List<String> errors = new ArrayList<>();

        if (doc.fiToFICstmrCdtTrf == null) {
            errors.add("FIToFICstmrCdtTrf missing");
            return errors;
        }

        validateNode(rulebook.get("GrpHdr"), doc.fiToFICstmrCdtTrf.grpHdr, "GrpHdr", doc, errors);

        if (doc.fiToFICstmrCdtTrf.cdtTrfTxInf != null) {
            for (int i = 0; i < doc.fiToFICstmrCdtTrf.cdtTrfTxInf.size(); i++) {
                Pacs008Document.CdtTrfTxInf tx = doc.fiToFICstmrCdtTrf.cdtTrfTxInf.get(i);
                validateNode(rulebook.get("CdtTrfTxInf"), tx, "Transaction[" + (i + 1) + "]", doc, errors);
            }
        } else {
            errors.add("No CdtTrfTxInf transactions found");
        }

        return errors;
    }

    private void validateNode(JsonNode ruleNode, Object obj, String path, Object parentObj, List<String> errors) throws IllegalAccessException {
        if (obj == null) {
            errors.add(path + " is missing");
            return;
        }

        for (Iterator<String> it = ruleNode.fieldNames(); it.hasNext(); ) {
            String fieldName = it.next();
            JsonNode rules = ruleNode.get(fieldName);

            if (isValidationKeyword(fieldName)) continue;

            String actualFieldName = fieldAliases.getOrDefault(fieldName, fieldName);

            try {
                Field field = obj.getClass().getDeclaredField(actualFieldName);
                field.setAccessible(true);
                Object value = field.get(obj);

                // ---------- Mandatory ----------
                if (rules.has("mandatory") && rules.get("mandatory").asBoolean()) {
                    if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                        errors.add(path + "." + fieldName + " is mandatory");
                    }
                }

                // ---------- Conditional Mandatory ----------
                if (rules.has("mandatoryIf") && rules.get("mandatoryIf").isTextual()) {
                    String condition = rules.get("mandatoryIf").asText();
                    if (evaluateCondition(condition, obj, parentObj) &&
                            (value == null || (value instanceof String && ((String) value).isEmpty()))) {
                        errors.add(path + "." + fieldName + " is mandatory due to condition: " + condition);
                    }
                }

                // ---------- List validation ----------
                if (value instanceof List<?>) {
                    List<?> list = (List<?>) value;

                    if (rules.has("maxItems") && list.size() > rules.get("maxItems").asInt()) {
                        errors.add(path + "." + fieldName + " exceeds maxItems=" + rules.get("maxItems").asInt());
                    }

                    if (rules.has("minItems") && list.size() < rules.get("minItems").asInt()) {
                        errors.add(path + "." + fieldName + " below minItems=" + rules.get("minItems").asInt());
                    }

                    // recurse into each element if it's an object
                    if (!list.isEmpty() && !isPrimitiveOrString(list.get(0).getClass()) && rules.isObject()) {
                        for (int i = 0; i < list.size(); i++) {
                            validateNode(rules, list.get(i), path + "." + fieldName + "[" + i + "]", obj, errors);
                        }
                    }
                    continue;
                }

                // ---------- String maxLength ----------
                if (value instanceof String && rules.has("maxLength")) {
                    int maxLength = rules.get("maxLength").asInt();
                    if (((String) value).length() > maxLength) {
                        errors.add(path + "." + fieldName + " exceeds maxLength=" + maxLength);
                    }
                }

                // ---------- Allowed values ----------
                if (rules.has("allowedValues") && value instanceof String) {
                    boolean match = false;
                    for (JsonNode allowed : rules.get("allowedValues")) {
                        if (allowed.asText().equals(value)) {
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        errors.add(path + "." + fieldName + " has invalid value: " + value);
                    }
                }

                // ---------- BIC ----------
                if (rules.has("bic") && value instanceof Pacs008Document.Agent) {
                    String bic = ((Pacs008Document.Agent) value).FinInstnId.BIC;
                    if (bic == null || !Pattern.compile("^[A-Z]{6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3})?$").matcher(bic).matches())
                        errors.add(path + "." + fieldName + " BIC invalid");
                }

                // ---------- IBAN ----------
                if (rules.has("iban") && value instanceof Pacs008Document.Account) {
                    String iban = ((Pacs008Document.Account) value).Id.IBAN;
                    if (iban == null || !Pattern.compile("^[A-Z]{2}\\d{2}[A-Z0-9]{1,30}$").matcher(iban).matches())
                        errors.add(path + "." + fieldName + " IBAN invalid");
                }

                // ---------- Currency ----------
                if (rules.has("Ccy") && value != null) {
                    String ccy = null;

                    if (value instanceof Pacs008Document.ActiveCurrencyAndAmount) {
                        ccy = ((Pacs008Document.ActiveCurrencyAndAmount) value).Ccy;
                    }

                    if (ccy == null || !Pattern.compile("^[A-Z]{3}$").matcher(ccy).matches()) {
                        errors.add(path + "." + fieldName + " has invalid currency code: " + ccy);
                    }
                }

                // ---------- Recurse nested objects ----------
                if (value != null && !isPrimitiveOrString(value.getClass()) && rules.isObject() && !(value instanceof List)) {
                    validateNode(rules, value, path + "." + fieldName, obj, errors);
                }

            } catch (NoSuchFieldException e) {
                errors.add(path + "." + fieldName + " not found in class " + obj.getClass().getSimpleName());
            }
        }
    }

    private boolean evaluateCondition(String condition, Object obj, Object parentObj) throws IllegalAccessException {
        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            String fieldName = parts[0].trim();
            String expectedValue = parts[1].trim().replaceAll("'", "");

            Object fieldValue = null;
            try {
                Field f = obj.getClass().getField(fieldName);
                fieldValue = f.get(obj);
            } catch (NoSuchFieldException ex) {
                try {
                    Field f = parentObj.getClass().getField(fieldName);
                    fieldValue = f.get(parentObj);
                } catch (NoSuchFieldException e) {
                    return false;
                }
            }

            return fieldValue != null && fieldValue instanceof String && expectedValue.equals(fieldValue);
        }
        return false;
    }

    private boolean isPrimitiveOrString(Class<?> clazz) {
        return clazz.isPrimitive() || clazz == String.class || Number.class.isAssignableFrom(clazz);
    }

    private boolean isValidationKeyword(String fieldName) {
        return "mandatory".equals(fieldName) ||
                "mandatoryIf".equals(fieldName) ||
                "iban".equals(fieldName) ||
                "bic".equals(fieldName) ||
                "maxItems".equals(fieldName) ||
                "minItems".equals(fieldName) ||
                "maxLength".equals(fieldName) ||
                "allowedValues".equals(fieldName) ||
                "Ccy".equals(fieldName);
    }
}
