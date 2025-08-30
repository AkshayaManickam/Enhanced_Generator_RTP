import * as yup from 'yup';

export const addAccountSchema = yup.object().shape({
  accountName: yup.string().required('Account Name is required'),
  accountType: yup.string().required('Account Type is required'),
  routingNumber: yup
    .string()
    .matches(/^\d{9}$/, 'Routing Number must be exactly 9 digits')
    .required('Routing Number is required'),
});
