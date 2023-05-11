#!/bin/bash

JAVA_HOME=$(/usr/libexec/java_home -v1.8)
export JAVA_HOME=$JAVA_HOME
export PATH=$JAVA_HOME/bin:$PATH
mvn clean package -Pdist -Dspark.version=3.1.3 -Djava.version=1.8

CLUSTER=bartek-spark-313s-on-dataproc

gcloud dataproc jobs submit spark --cluster=${CLUSTER} --region=us-central1 \
--class=com.bawi.spark.MyReadAvroGcsAndWriteBQBroadcastApp \
--jars=target/my-apache-spark-3-scala-0.1-SNAPSHOT.jar \
--labels=job_name=bartek-myreadavrogcsandwritebqbroadcastapp \
--properties ^#^spark.jars.packages=org.apache.spark:spark-avro_2.12:3.1.3,com.google.cloud.spark:spark-bigquery-with-dependencies_2.12:0.29.0#spark.dynamicAllocation.enabled=true#spark.shuffle.service.enabled=true#spark.metrics.conf.*.sink.mygcpmetric.class=org.apache.spark.metrics.sink.MyGcpMetricSink \
-- \
 --projectId=${GCP_PROJECT}

echo "Waiting 10 secs for logs to appear in GCP Logs Explorer"
SLEEP 10

LABELS_JOB_NAME=bartek-myreadavrogcsandwritebqbroadcastapp && \
START_TIME="$(date -u -v-1S '+%Y-%m-%dT%H:%M:%SZ')" && \
END_TIME="$(date -u -v-10M '+%Y-%m-%dT%H:%M:%SZ')" && \
LATEST_JOB_ID=$(gcloud dataproc jobs list --region=us-central1 --filter="placement.clusterName=${CLUSTER} AND labels.job_name=${LABELS_JOB_NAME}" --format=json --sort-by=~status.stateStartTime | jq -r ".[0].reference.jobId") && \
echo "Latest job id: $LATEST_JOB_ID" &&
gcloud logging read --project ${GCP_PROJECT} "timestamp<=\"${START_TIME}\" AND timestamp>=\"${END_TIME}\" AND resource.type=cloud_dataproc_job AND labels.\"dataproc.googleapis.com/cluster_name\"=${CLUSTER} AND resource.labels.job_id=${LATEST_JOB_ID} AND jsonPayload.message=~\".*MyReadAvroGcsAndWriteBQBroadcastApp.*\"" --format "table(timestamp,jsonPayload.message)" --order=asc



gcloud dataproc jobs submit spark --cluster=${CLUSTER} --region=us-central1 \
--class=com.bawi.spark.MyMultiOutputMetricsApp \
--jars=target/my-apache-spark-3-scala-0.1-SNAPSHOT.jar \
--labels=job_name=bartek-mymultioutputmetricsapp \
--properties ^#^spark.jars.packages=org.apache.spark:spark-avro_2.12:3.1.3#spark.dynamicAllocation.enabled=true#spark.shuffle.service.enabled=true#spark.metrics.conf.*.sink.myconsole.class=org.apache.spark.metrics.sink.MyConsoleSink \
-- \
 --projectId=${GCP_PROJECT}

echo "Waiting 10 secs for logs to appear in GCP Logs Explorer"
SLEEP 10

LABELS_JOB_NAME=bartek-mymultioutputmetricsapp && \
START_TIME="$(date -u -v-1S '+%Y-%m-%dT%H:%M:%SZ')" && \
END_TIME="$(date -u -v-10M '+%Y-%m-%dT%H:%M:%SZ')" && \
LATEST_JOB_ID=$(gcloud dataproc jobs list --region=us-central1 --filter="placement.clusterName=${CLUSTER} AND labels.job_name=${LABELS_JOB_NAME}" --format=json --sort-by=~status.stateStartTime | jq -r ".[0].reference.jobId") && \
echo "Latest job id: $LATEST_JOB_ID" &&
gcloud logging read --project ${GCP_PROJECT} "timestamp<=\"${START_TIME}\" AND timestamp>=\"${END_TIME}\" AND resource.type=cloud_dataproc_job AND labels.\"dataproc.googleapis.com/cluster_name\"=${CLUSTER} AND resource.labels.job_id=${LATEST_JOB_ID} AND jsonPayload.message=~\".*MyMultiOutputMetricsApp.*\"" --format "table(timestamp,jsonPayload.message)" --order=asc
