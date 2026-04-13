#!/bin/bash
set -e

export PATH="/Users/ir-home/google-cloud-sdk/bin:$PATH"
GCLOUD=/Users/ir-home/google-cloud-sdk/bin/gcloud

PROJECT_ID="quiz-trainer-491807"
REGION="us-central1"
SERVICE_NAME="quiz-service"
REPO="quiz-repo"
IMAGE="$REGION-docker.pkg.dev/$PROJECT_ID/$REPO/$SERVICE_NAME:latest"
CLOUD_SQL_INSTANCE="$PROJECT_ID:$REGION:ai-trainer-project"

echo "=== Setting GCP project ==="
$GCLOUD config set project $PROJECT_ID

echo "=== Creating Artifact Registry repo (if not exists) ==="
$GCLOUD artifacts repositories create $REPO \
  --repository-format=docker \
  --location=$REGION \
  --description="Quiz service Docker images" 2>/dev/null || echo "Repo already exists"

echo "=== Configuring Docker auth for Artifact Registry ==="
$GCLOUD auth configure-docker $REGION-docker.pkg.dev --quiet

echo "=== Building Spring Boot jar ==="
cd "$(dirname "$0")"
./mvnw clean package -DskipTests

echo "=== Building Docker image ==="
docker build -t $IMAGE .

echo "=== Pushing image to Artifact Registry ==="
docker push $IMAGE

echo "=== Deploying to Cloud Run ==="
$GCLOUD run deploy $SERVICE_NAME \
  --image=$IMAGE \
  --platform=managed \
  --region=$REGION \
  --allow-unauthenticated \
  --port=8080 \
  --memory=512Mi \
  --cpu=1 \
  --min-instances=0 \
  --max-instances=3 \
  --add-cloudsql-instances=$CLOUD_SQL_INSTANCE \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod,DB_USER=quiz_user,DB_PASSWORD=quiz_pass"

echo ""
echo "=== Deployment complete! ==="
$GCLOUD run services describe $SERVICE_NAME --region=$REGION --format="value(status.url)"
