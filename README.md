# See setup-cluster.sh or setup-cluster-with-custom-sink.sh
# then run ./run.sh

export JAVA_HOME=$(/usr/libexec/java_home -v1.8)
export PATH=$JAVA_HOME/bin:$PATH
mvn -version
mvn dependency:tree -Dverbose > dep.txt

CLUSTER=bartek-spark-313s-on-dataproc
# gcloud compute ssh --zone "${GCP_ZONE}" "${CLUSTER}-m" --tunnel-through-iap --project "${GCP_PROJECT}" -- "find / -iname \*protobuf-java-util\*.jar"

# applicationId=application_1683...75114_0008
# gcloud compute ssh --zone "${GCP_ZONE}" "${CLUSTER}-m" --tunnel-through-iap --project "${GCP_PROJECT}" -- "yarn logs -applicationId $applicationId" > $applicationId.log.txt

gcloud dataproc jobs submit spark --cluster=${CLUSTER} --region=us-central1 \
--class=com.bawi.spark.MySimpleCountApp \
--jars=gs://${GCP_PROJECT}-bartek-dataproc/my-apache-spark-3-scala-0.1-SNAPSHOT.jar \
--labels=job_name=bartek-mysimplecountapp

gcloud dataproc jobs submit spark --cluster=${CLUSTER} --region=us-central1 \
--class=com.bawi.spark.MyReadAvroGcsApp \
--jars=gs://${GCP_PROJECT}-bartek-dataproc/my-apache-spark-3-scala-0.1-SNAPSHOT.jar \
--properties spark.jars.packages=org.apache.spark:spark-avro_2.12:3.1.3 \
--labels=job_name=bartek-myreadavrogcsapp \
-- \
 --projectId=${GCP_PROJECT}


Logging:
# query spark container logs in logs explorer
resource.labels.job_id="4220aac1746d4d5195e69e..."
resource.type="cloud_dataproc_job"
(jsonPayload.class=~"com.*" OR jsonPayload.class=~"org.*")

LABELS_JOB_NAME=bartek-mysimplecountapp && \

LABELS_JOB_NAME=bartek-myreadavrogcsapp && \
START_TIME="$(date -u -v-1S '+%Y-%m-%dT%H:%M:%SZ')" && \
END_TIME="$(date -u -v-60M '+%Y-%m-%dT%H:%M:%SZ')" && \
LATEST_JOB_ID=$(gcloud dataproc jobs list --region=us-central1 --filter="placement.clusterName=${CLUSTER} AND labels.job_name=${LABELS_JOB_NAME}" --format=json --sort-by=~status.stateStartTime | jq -r ".[0].reference.jobId") && \
echo "Latest job id: $LATEST_JOB_ID" && 
gcloud logging read --project ${GCP_PROJECT} "timestamp<=\"${START_TIME}\" AND timestamp>=\"${END_TIME}\" AND resource.type=cloud_dataproc_job AND labels.\"dataproc.googleapis.com/CLUSTER\"=${CLUSTER} AND resource.labels.job_id=${LATEST_JOB_ID} AND (jsonPayload.class=~\"com.*\" OR jsonPayload.class=~\"org.*\")" --format "table(timestamp,jsonPayload.message)" --order=asc


TIMESTAMP             MESSAGE
2023-04-12T12:59:33Z  GOOGLE_APPLICATION_CREDENTIALS=null
2023-04-12T12:59:33Z  Setting Spark conf: [spark.eventLog.enabled=true, spark.dynamicAllocation.minExecutors=1, spark.sql.autoBroadcastJoinThreshold=46m, spark.jars=file:/tmp/4220aac1746d4d5195e69ec649459d06/dataproc-empty-jar-1681304370433.jar, spark.master.rest.enabled=true, spark.dataproc.sql.parquet.enableFooterCache=true, spark.driver.maxResultSize=2048m, spark.dataproc.sql.joinConditionReorder.enabled=true, spark.yarn.am.memory=640m, spark.yarn.historyServer.address=${CLUSTER}-m:18080, spark.dataproc.sql.local.rank.pushdown.enabled=true, spark.executor.instances=2, spark.repl.local.jars=file:///tmp/4220aac1746d4d5195e69ec649459d06/my-apache-spark-3-scala-0.1-SNAPSHOT.jar,file:///root/.ivy2/jars/org.apache.spark_spark-avro_2.12-3.1.3.jar,file:///root/.ivy2/jars/com.google.cloud.spark_spark-bigquery-with-dependencies_2.12-0.29.0.jar,file:///root/.ivy2/jars/org.spark-project.spark_unused-1.0.0.jar, spark.yarn.unmanagedAM.enabled=true, spark.submit.deployMode=client, spark.dataproc.metrics.listener.metrics.collector.hostname=${CLUSTER}-m, spark.extraListeners=com.google.cloud.spark.performance.DataprocMetricsListener, spark.yarn.dist.jars=file:///tmp/4220aac1746d4d5195e69ec649459d06/my-apache-spark-3-scala-0.1-SNAPSHOT.jar,file:///root/.ivy2/jars/org.apache.spark_spark-avro_2.12-3.1.3.jar,file:///root/.ivy2/jars/com.google.cloud.spark_spark-bigquery-with-dependencies_2.12-0.29.0.jar,file:///root/.ivy2/jars/org.spark-project.spark_unused-1.0.0.jar, spark.app.name=com.bawi.spark.MyReadAvroGcsAndWriteBQBroadcastApp$, spark.driver.memory=4096m, spark.history.fs.logDirectory=gs://dataproc-temp-us-central1-318621067355-ph9xjy72/f02f6f56-1570-4245-8af2-05e726693604/spark-job-history, spark.sql.cbo.joinReorder.enabled=true, spark.shuffle.service.enabled=true, spark.metrics.namespace=app_name:${spark.app.name}.app_id:${spark.app.id}, spark.scheduler.mode=FAIR, spark.dataproc.sql.optimizer.leftsemijoin.conversion.enabled=true, spark.sql.adaptive.enabled=true, spark.yarn.jars=local:/usr/lib/spark/jars/*, spark.eventLog.dir=gs://dataproc-temp-us-central1-318621067355-ph9xjy72/f02f6f56-1570-4245-8af2-05e726693604/spark-job-history, spark.scheduler.minRegisteredResourcesRatio=0.0, spark.yarn.tags=dataproc_hash_161b47e9-947c-355a-9d24-b7583d80e90b,dataproc_job_4220aac1746d4d5195e69ec649459d06,dataproc_master_index_0,dataproc_uuid_e7a7d831-a388-3eb6-a5df-6a3d90762aaf, spark.hadoop.hive.execution.engine=mr, spark.executor.cores=2, spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version=2, spark.dynamicAllocation.maxExecutors=10000, spark.jars.packages=org.apache.spark:spark-avro_2.12:3.1.3,com.google.cloud.spark:spark-bigquery-with-dependencies_2.12:0.29.0, spark.master=yarn, spark.ui.port=0, spark.sql.catalogImplementation=hive, spark.rpc.message.maxSize=512, spark.executor.memory=6157m, projectId=my-project, spark.executorEnv.OPENBLAS_NUM_THREADS=1, spark.submit.pyFiles=, spark.dynamicAllocation.enabled=true, spark.sql.cbo.enabled=true]
2023-04-12T12:59:34Z  Logging initialized @3384ms to org.sparkproject.jetty.util.log.Slf4jLog
2023-04-12T12:59:34Z  jetty-9.4.40.v20210413; built: 2021-04-13T20:42:42.668Z; git: b881a572662e1943a14ae12e7e1207989f218b74; jvm 1.8.0_362-b09
2023-04-12T12:59:34Z  Connecting to ResourceManager at ${CLUSTER}-m/10.128.124.245:8032
2023-04-12T12:59:34Z  Connecting to Application History server at ${CLUSTER}-m/10.128.124.245:10200
2023-04-12T12:59:36Z  Submitted application application_1680791486132_0024
2023-04-12T12:59:46Z  |Querying table my-project.my_dataset._bqc_f254984e78ba42d59b15497e8b1a8755, parameters sent from Spark:|requiredColumns=[name,uname],|filters=[]
2023-04-12T12:59:48Z  Read session:{"readSessionName":"projects/my-project/locations/us/sessions/CAISDHIzTElMUDJzbG51dRoCamkaAmlj","readSessionCreationStartTime":"2023-04-12T12:59:46.395Z","readSessionCreationEndTime":"2023-04-12T12:59:48.612Z","readSessionPrepDuration":939,"readSessionCreationDuration":1278,"readSessionDuration":2217}
2023-04-12T12:59:48Z  Requested 20000 max partitions, but only received 1 from the BigQuery Storage API for session projects/my-project/locations/us/sessions/CAISDHIzTElMUDJzbG51dRoCamkaAmlj. Notice that the number of streams in actual may be lower than the requested number, depending on the amount parallelism that is reasonable for the table and the maximum amount of parallelism allowed by the system.
2023-04-12T12:59:48Z  Created read session for table 'my-project.my-dataset._bqc_f254984e78ba42d59b15497e8b1a8755': projects/my-project/locations/us/sessions/CAISDHIzTElMUDJzbG51dRoCamkaAmlj
2023-04-12T12:59:52Z  Debug mode disabled.
2023-04-12T12:59:52Z  allocation manager type not specified, using netty as the default type
2023-04-12T12:59:52Z  Using DefaultAllocationManager at memory/DefaultAllocationManagerFactory.class
2023-04-12T12:59:52Z  Tracer Logs:{"Stream Name":"projects/my-project/locations/us/sessions/CAISDHIzTElMUDJzbG51dRoCamkaAmlj/streams/GgJqaRoCaWMoAg","Started":"2023-04-12T12:59:50.569Z","Ended":"2023-04-12T12:59:52.879Z","Parse Timings":"Average: PT0.626029586S Samples: 1","Time in Spark":"Not enough samples.","Time waiting for service":"Average: PT0.259270395S Samples: 1","Bytes/s":1853,"Rows/s":27,"Bytes":480,"Rows":17,"I/O time":259}
2023-04-12T12:59:56Z  Compression: SNAPPY
2023-04-12T12:59:56Z  Compression: SNAPPY
2023-04-12T12:59:56Z  Parquet block size to 134217728
2023-04-12T12:59:56Z  Parquet page size to 1048576
2023-04-12T12:59:56Z  Parquet dictionary page size to 1048576
2023-04-12T12:59:56Z  Dictionary is on
2023-04-12T12:59:56Z  Validation is off
2023-04-12T12:59:56Z  Writer version is: PARQUET_1_0
2023-04-12T12:59:56Z  Maximum row group padding size is 8388608 bytes
2023-04-12T12:59:56Z  Page size checking is: estimated
2023-04-12T12:59:56Z  Min row count for page size check is: 100
2023-04-12T12:59:56Z  Max row count for page size check is: 10000
2023-04-12T12:59:56Z  Truncate length for column indexes is: 64
2023-04-12T12:59:56Z  Truncate length for statistics min/max  is: 2147483647
2023-04-12T12:59:56Z  Page row count limit to 20000
2023-04-12T12:59:56Z  Writing page checksums is: on
2023-04-12T12:59:56Z  Got brand-new compressor [.snappy]
2023-04-12T12:59:58Z  processing (Bob,abc,BOB)
2023-04-12T12:59:59Z  Successfully repaired 'gs://my-project-bartek-spark/.spark-bigquery-application_1680791486132_0024-433eac2f-af74-4542-a01c-5a2330489dc1/_temporary/0/_temporary/attempt_202304121259548583825413632832218_0001_m_000000_1/' directory.
2023-04-12T13:00:00Z  Successfully repaired 'gs://my-project-bartek-spark/.spark-bigquery-application_1680791486132_0024-433eac2f-af74-4542-a01c-5a2330489dc1/_temporary/0/_temporary/' directory.
2023-04-12T13:00:01Z  Successfully repaired 'gs://my-project-bartek-spark/.spark-bigquery-application_1680791486132_0024-433eac2f-af74-4542-a01c-5a2330489dc1/' directory.
2023-04-12T13:00:02Z  Submitted job LoadJobConfiguration{type=LOAD, destinationTable=GenericData{classInfo=[datasetId, projectId, tableId], {datasetId=my_dataset, projectId=my-project, tableId=my_table}}, decimalTargetTypes=null, destinationEncryptionConfiguration=null, createDisposition=CREATE_IF_NEEDED, writeDisposition=WRITE_APPEND, formatOptions=FormatOptions{format=PARQUET}, nullMarker=null, maxBadRecords=null, schema=Schema{fields=[Field{name=name, type=STRING, mode=NULLABLE, description=null, policyTags=null, maxLength=null, scale=null, precision=null, defaultValueExpression=null, collation=null}, Field{name=body, type=BYTES, mode=NULLABLE, description=null, policyTags=null, maxLength=null, scale=null, precision=null, defaultValueExpression=null, collation=null}, Field{name=uname, type=STRING, mode=NULLABLE, description=null, policyTags=null, maxLength=null, scale=null, precision=null, defaultValueExpression=null, collation=null}]}, ignoreUnknownValue=null, sourceUris=[gs://my-project-bartek-spark/.spark-bigquery-application_1680791486132_0024-433eac2f-af74-4542-a01c-5a2330489dc1/part-00000-70d7b4c2-887b-447b-aa42-c454aa7c3a95-c000.snappy.parquet], schemaUpdateOptions=null, autodetect=null, timePartitioning=null, clustering=null, useAvroLogicalTypes=null, labels=null, jobTimeoutMs=null, rangePartitioning=null, hivePartitioningOptions=null, referenceFileSchemaUri=null}. jobId: JobId{project=my-project, job=3d6a6ddd-9ff1-4852-b451-668869fdbaee, location=US}
2023-04-12T13:00:03Z  Done loading to my-project.my_dataset.my_table. jobId: JobId{project=my-project, job=3d6a6ddd-9ff1-4852-b451-668869fdbaee, location=US}
2023-04-12T13:00:04Z  Stopped Spark@590b0f47{HTTP/1.1, (http/1.1)}{0.0.0.0:0}


# customize multiple properties for same and different components
# https://stackoverflow.com/questions/72369619/gcp-dataproc-adding-multiple-packageskafka-mongodb-while-submitting-jobs-no
# https://medium.com/@randy-huang/gcp-dataproc-using-customize-properties-18f83a9ead06
