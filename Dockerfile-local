FROM registry.ap-southeast-2.aliyuncs.com/fengkong/maven:3-jdk-11 as builder
WORKDIR /data
COPY . .
RUN mvn clean package -B -DskipTests


FROM registry.ap-southeast-2.aliyuncs.com/fengkong/openjdk:11-jre
ENV TZ Asia/Shanghai
WORKDIR /data
COPY --from=builder /data/target/ROOT.jar .
CMD ["java","-jar","ROOT.jar"]