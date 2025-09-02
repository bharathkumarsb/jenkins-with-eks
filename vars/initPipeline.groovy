/**
 * Shared library step: Initializes Jenkins pipeline, workspace, and environment variables.
 * Usage: `initPipeline()`
 */
def call() {
    stage('Initialize') {
        echo "Initializing pipeline for ${env.JOB_NAME}"
        checkout scm
        // Load any shared environment config, secrets, etc.
        env.BUILD_TAG = "${env.GIT_COMMIT.take(7)}"
        // Other initialization as needed
    }
}