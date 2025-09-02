// Example Job DSL script for creating modular jobs and folders

folder('platform') {
    displayName('Platform Pipelines')
}

folder('services') {
    displayName('Service Pipelines')
}

job('platform/eks-env-manage') {
    description('Manage EKS clusters and namespaces')
    // Add steps for EKS management here...
}

job('platform/base-image-build') {
    description('Build and publish base Docker images')
    // Add steps for base image build...
}

job('services/hello-svc-ci') {
    description('CI for hello-svc')
    scm {
        git('https://github.com/bharathkumarsb/jenkins-with-eks.git')
    }
    steps {
        shell('jenkinsfile.ci')
    }
}

job('services/hello-svc-deploy') {
    description('Deploy hello-svc')
    scm {
        git('https://github.com/bharathkumarsb/jenkins-with-eks.git')
    }
    steps {
        shell('jenkinsfile.deploy')
    }
}