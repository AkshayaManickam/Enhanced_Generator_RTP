@Library('jenkins-shared-lib') _

standardPipelineAppJDK21 {
    SERVICE_NAME              = 'galaxy-rtp-validator-service'
    PRODUCT_NAME              = 'bankos'
    S3UPLOAD                  = 'false'
    RELEASE                   = '2.0'
    metadata_file_name        = 'galaxy-rtp-validator-service.yaml'
    upstreamDependencies      = 'galaxy-common-sdk,galaxy-plugins'
    unitTestEnabled           = 'true'
}
