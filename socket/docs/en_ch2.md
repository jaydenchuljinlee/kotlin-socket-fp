# 📦 Apache Cassandra 학습 정리

## 📌 Cassandra란?

**Apache Cassandra**는 대용량의 데이터를 다루기 위한 **분산형 NoSQL 데이터베이스**입니다. 고가용성과 확장성에 최적화되어 있으며, 글로벌 서비스를 운영하는 기업에서 널리 사용됩니다.

---

## ✅ Cassandra의 핵심 특징

| 항목 | 설명 |
|------|------|
| **NoSQL** | 스키마 유연성. 관계형 조인, 트랜잭션 없음 |
| **Wide-column Store** | 테이블의 각 행이 동적으로 서로 다른 컬럼을 가질 수 있음 |
| **분산형 구조 (Masterless)** | 모든 노드가 동등하며, 장애 시 자동으로 복구 가능 |
| **높은 쓰기 성능** | WAL → Memtable → SSTable 구조로 빠른 쓰기 성능 보장 |
| **확장성** | 노드를 추가하면 자동으로 리밸런싱, 무중단 확장 |
| **튜닝 가능한 일관성** | ONE, QUORUM, ALL 등으로 조절 가능 (CAP 중 AP 지향) |

---

## 🧩 Cassandra 기본 구조 예시

```sql
CREATE TABLE user_messages (
    user_id UUID,
    message_id TIMEUUID,
    message_text TEXT,
    PRIMARY KEY (user_id, message_id)
) WITH CLUSTERING ORDER BY (message_id DESC);
```
→ 특정 유저의 최근 메시지를 빠르게 조회하는 구조.

## ⚙️ Spring Boot 3.2 + Cassandra 연동

### 1. Gradle 의존성 추가

```groovy
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-cassandra")
}
```

### 2. application.yml 설정

```yaml
spring:
  data:
    cassandra:
      contact-points: localhost
      port: 9042
      keyspace-name: my_keyspace
      local-datacenter: datacenter1
      schema-action: create_if_not_exists
```

### 3. Cassandra 초기화 (init.cql)

```sql
CREATE KEYSPACE IF NOT EXISTS my_keyspace 
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
```

### 4. docker-compose.yml 예시

```yaml
version: '3.8'
services:
  cassandra:
    image: cassandra:4.1
    ports:
      - "9042:9042"
    volumes:
      - ./init:/init
    command: >
      bash -c "
        /usr/local/bin/docker-entrypoint.sh cassandra &
        echo 'Waiting for Cassandra to be ready...' &&
        until cqlsh -e 'SHOW VERSION'; do sleep 5; done &&
        cqlsh -f /init/init.cql &&
        tail -f /dev/null
      "
```

> Spring Boot는 Cassandra가 준비된 이후에 기동되어야 함
wait-for-it.sh 또는 Health Check를 활용한 순서 보장이 필요

## 🔍 Cassandra vs 다른 분산 저장소 비교

| 항목 | **Cassandra** | **MongoDB** | **HBase** | **DynamoDB** |
|------|---------------|-------------|-----------|--------------|
| **데이터 모델** | Wide-column | Document | Column-family (Hadoop 기반) | Key-Value |
| **스키마 유연성** | 높음 (컬럼 추가 자유) | 높음 | 낮음 (명시 필요) | 높음 |
| **확장성** | 뛰어남 (무중단 노드 확장, P2P 구조) | 좋음 (Sharding) | 매우 뛰어남 (HDFS 기반) | 뛰어남 (완전관리형) |
| **일관성 모델** | 선택적 (ONE ~ ALL) / AP 지향 | CP 지향 (기본 strong) | CP (strong consistency) | 선택적 (eventual ~ strong) |
| **쓰기 성능** | 매우 뛰어남 (SSTable 기반) | 좋음 | 매우 좋음 | 좋음 |
| **읽기 성능** | 패턴에 따라 좋음 | 좋음 | 좋음 | 매우 좋음 |
| **조인/그룹 쿼리** | ❌ 없음 (쿼리 기반 모델링) | ✅ 있음 (제한적) | ❌ 없음 | ❌ 없음 |
| **복잡한 쿼리** | 제한적 (PK/Index 기반) | 비교적 가능 | 매우 제한적 | 매우 제한적 |
| **장애 복구** | 자동 복제 + 노드 장애 대응 탁월 | ReplicaSet, Sharding | 복제 기반 | AWS가 관리 |
| **운영 난이도** | 높음 (튜닝 필요) | 낮음 (편리한 도구 제공) | 매우 높음 (Hadoop 기반) | 매우 낮음 (서버리스) |
| **오픈소스 여부** | ✅ | ✅ | ✅ | ❌ (AWS 전용) |

---

## 💡 Cassandra의 특장점 요약

- **무중단 확장 가능**: 수평 확장이 매우 쉽고, 운영 중에도 가능
- **Masterless 구조**: 모든 노드가 동일한 역할 → **장애 복구에 강함**
- **조절 가능한 일관성**: 필요에 따라 강한 일관성 또는 고가용성 선택 가능
- **쓰기 성능 우수**: WAL → Memtable → SSTable 구조로 빠른 쓰기 처리
- **Time-series, 대규모 메시지 처리에 적합**

---

## 🧭 어떤 상황에서 Cassandra가 적합한가?

| 상황 | 적합 여부 | 비고 |
|------|-----------|------|
| 대규모 쓰기 처리 (로그, 센서 데이터 등) | ✅ 매우 적합 | 빠른 쓰기 처리 필요 |
| 글로벌 사용자 상태 저장 | ✅ 적합 | 지연 없는 다중 노드 구조 |
| 복잡한 쿼리/조인/검색 필요 | ❌ 부적합 | RDB 또는 Elasticsearch 추천 |
| 단순 Key-Value 캐시 | ❌ 대체 가능 | Redis, DynamoDB가 더 적합 |
| 완전관리형 서비스 필요 | ❌ 직접 운영 필요 | DynamoDB 또는 Cloud Bigtable 고려 |

---

