# HelloWorld Java Application on SAP BTP Cloud Foundry

A simple Java application deployed on SAP Business Technology Platform (BTP) Cloud Foundry environment with authentication and authorization using XSUAA service.

## Project Overview

This project demonstrates:
- Java application deployment on SAP BTP Cloud Foundry
- Spring Boot REST API implementation
- Authentication using XSUAA service
- Authorization with role-based access control
- Application router configuration for SSO

## Prerequisites

- SAP BTP account (Trial or Enterprise)
- Cloud Foundry CLI installed
- Java 21 JDK
- Maven 3.8+
- Node.js (for approuter)

# HelloWorld Java Application on SAP BTP Cloud Foundry

A simple Java application deployed on SAP Business Technology Platform (BTP) Cloud Foundry environment with authentication and authorization using XSUAA service.

## Project Overview

This project demonstrates:
- Java application deployment on SAP BTP Cloud Foundry
- Spring Boot REST API implementation
- Authentication using XSUAA service
- Authorization with role-based access control
- Application router configuration for SSO

## Prerequisites

- SAP BTP account (Trial or Enterprise)
- Cloud Foundry CLI installed
- Java 21 JDK
- Maven 3.8+
- Node.js (for approuter)

# Build Java application
mvn clean install

# Create XSUAA service instance
cf create-service xsuaa application javauaa -c xs-security.json

# Deploy applications
cf push

# After code changes, rebuild and redeploy
mvn clean install
cf push

# To update just the Java application
cf push helloworld

# To update just the approuter
cf push web

