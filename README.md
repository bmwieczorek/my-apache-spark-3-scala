export JAVA_HOME=$(/usr/libexec/java_home -v1.8)
export PATH=$JAVA_HOME/bin:$PATH
mvn -version
mvn clean package -Pdist
mvn dependency:tree -Dverbose > dep.txt


gsutil rm gs://${GCP_PROJECT}-bartek-dataproc/my-apache-spark-3-scala-0.1-SNAPSHOT.jar
mvn clean package -Pdist && gsutil cp target/my-apache-spark-3-scala-0.1-SNAPSHOT.jar gs://${GCP_PROJECT}-bartek-dataproc/

gcloud dataproc jobs submit spark --cluster=bartek-beam-on-spark --region=us-central1 \
--class=com.bawi.MySimpleCountApp \
--jars=gs://${GCP_PROJECT}-bartek-dataproc/my-apache-spark-3-scala-0.1-SNAPSHOT.jar \
--labels=job_name=bartek-mysimplecountapp

gcloud dataproc jobs submit spark --cluster=bartek-beam-on-spark --region=us-central1 \
--class=com.bawi.MyReadAvroGcsApp \
--jars=gs://${GCP_PROJECT}-bartek-dataproc/my-apache-spark-3-scala-0.1-SNAPSHOT.jar \
--properties spark.jars.packages=org.apache.spark:spark-avro_2.12:3.1.3 \
--labels=job_name=bartek-myreadavrogcsapp \
-- \
 --projectId=${GCP_PROJECT}

gcloud dataproc jobs submit spark --cluster=bartek-beam-on-spark --region=us-central1 \
--class=com.bawi.MyReadAvroGcsAndWriteBQBroadcastApp \
--jars=gs://${GCP_PROJECT}-bartek-dataproc/my-apache-spark-3-scala-0.1-SNAPSHOT.jar \
--labels=job_name=bartek-myreadavrogcsandwritebqbroadcastapp \
--properties ^#^spark.jars.packages=org.apache.spark:spark-avro_2.12:3.1.3,com.google.cloud.spark:spark-bigquery-with-dependencies_2.12:0.29.0#spark.dynamicAllocation.enabled=true#spark.shuffle.service.enabled=true \
-- \
 --projectId=${GCP_PROJECT}

# customize multiple properties for same and different components 
# https://stackoverflow.com/questions/72369619/gcp-dataproc-adding-multiple-packageskafka-mongodb-while-submitting-jobs-no
# https://medium.com/@randy-huang/gcp-dataproc-using-customize-properties-18f83a9ead06
