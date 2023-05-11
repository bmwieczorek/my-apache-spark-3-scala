#!/bin/bash
CLUSTER=bartek-spark-313s-on-dataproc

gcloud dataproc clusters delete ${CLUSTER} --project ${GCP_PROJECT} --region us-central1 --quiet
gsutil -m rm -r gs://${GCP_PROJECT}-${CLUSTER}
gsutil mb -l ${GCP_REGION} gs://${GCP_PROJECT}-${CLUSTER}

gcloud dataproc clusters create ${CLUSTER} \
--project ${GCP_PROJECT} --region us-central1 --zone="" --no-address \
--subnet ${GCP_SUBNETWORK} \
--master-machine-type t2d-standard-4 --master-boot-disk-size 1000 \
--num-workers 2 --worker-machine-type t2d-standard-4 --worker-boot-disk-size 2000 \
--image-version 2.0 \
--scopes 'https://www.googleapis.com/auth/cloud-platform' \
--service-account=${GCP_SERVICE_ACCOUNT} \
--bucket ${GCP_PROJECT}-${CLUSTER} \
--optional-components DOCKER \
--enable-component-gateway \
--properties spark:spark.master.rest.enabled=true,dataproc:dataproc.logging.stackdriver.job.driver.enable=true,dataproc:dataproc.logging.stackdriver.enable=true,dataproc:jobs.file-backed-output.enable=true,dataproc:dataproc.logging.stackdriver.job.yarn.container.enable=true \
--metric-sources=spark,hdfs,yarn,spark-history-server,hiveserver2,hivemetastore,monitoring-agent-defaults
