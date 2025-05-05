# Kotlin 프로젝트에서의 `Either`와 `Validated` 적용 정리

## ✅ Either와 Validated란?

### 1. `Either`

- 두 개의 가능한 값 중 **하나**를 담는 컨테이너 타입.
- 보통 `Left`는 **실패**, `Right`는 **성공**을 의미함.

```kotlin
val result: Either<Error, Success> = Either.Right(Success(...))```

- `fold(left, right)`를 통해 결과를 분기 처리 가능.
- 단점: `Left` 또는 `Right` 중 하나만 존재하므로, 여러 에러를 담기 어려움.

### 1. `Validated`
- `Either`와 비슷하지만, 여러 개의 에러를 누적할 수 있음.
- 주로 유효성 검증에 사용.

```kotlin
val result: ValidatedNel<ValidationError, ValidatedUser>```

- `Validated`는 성공(Valid) 또는 실패(Invalid(errors)) 중 하나를 가짐.
- `Nel`: Non-empty list (1개 이상의 오류를 담을 수 있음)

## ✅ 왜 현재 프로젝트에 적용했는가?

### 1. 명확한 성공/실패 분기 처리

```kotlin
chatService.executeCommand(command)
    .fold(
        ifLeft = { MessageResponse("[Error] ${it.toMessage()}") },
        ifRight = { it }
    )```

### 2. 일관된 컨트롤러 처리
- 모든 요청이 `Either`로 처리되므로 코드의 <b>통일성과 안정성</b> 확보

### 3. 불변 상태 기반 아키텍처에 적합
- `ChatCommand → ChatEffect → runEffects` 구조에서, 상태를 직접 변경하지 않고 새 상태를 리턴
- 실패도 side-effect 없이 처리
 
## ✅ Kotlin의 suspend 및 비동기와의 관계
- Arrow의 Either는 suspend 함수 내에서도 자연스럽게 사용 가능
- Validated는 주로 동기 검증에 사용됨

```kotlin
suspend fun execute(): Either<AppError, Response> {
    return if (success) Either.Right(response)
           else Either.Left(AppError("..."))
}```

- 코루틴 환경에서도 Either는 완벽하게 작동

## ✅ 함수형 프로그래밍에서의 장점

| 항목     | Either                                  | Validated                                 |
|----------|-----------------------------------------|--------------------------------------------|
| 목적     | 성공/실패 처리                          | 다중 검증 결과 누적                        |
| 처리 방식| `.fold {}` 통한 분기                    | `.mapN`, `.combine` 통한 병합              |
| 순수성   | 상태 변경 없이 새로운 값 리턴           | 예측 가능한 결과 리턴                      |
| 테스트성 | side-effect 분리 → 테스트 용이           | 에러 케이스 검증이 쉬움                    |

함수형 스타일로 작성하면 다음과 같은 장점이 있습니다:

- 💡 명령과 효과 분리 → side-effect 없는 순수 로직 가능  
- 🔄 상태 변경 없이 새로운 상태를 리턴 → 불변성 유지  
- ✅ 예외 대신 값으로 에러를 다루기 때문에 흐름이 예측 가능함  
- 🧪 테스트 및 디버깅이 쉬움 → 로직 단위 검증 가능  

---

## ❌ 단점

| 항목       | 단점 설명 |
|------------|-----------|
| Either     | - 에러 누적이 불가<br>- 체이닝 시 코드 길어짐 |
| Validated  | - `suspend` 및 코루틴과 병행하기 어려움<br>- 제한된 사용처 (입력 검증 등) |
| 공통       | - 러닝 커브 존재 (객체지향 개발자에겐 익숙하지 않을 수 있음)<br>- 잘못 사용 시 오히려 복잡도 증가 가능 |

---

## ✅ 결론 요약

| 항목 | 설명 |
|------|------|
| Either | 에러와 성공을 명확히 구분하며, 비동기/코루틴 환경에서도 적합 |
| Validated | 다중 유효성 검증에 강력한 도구 |
| 현재 적용 이유 | 명령-이벤트-상태 전이 구조를 안전하고 명확하게 구현하기 위함 |
| 함수형 스타일과의 궁합 | 테스트 용이성, 예측 가능성, 유지보수성 증가 |
| 주의점 | 남용하거나 도구에 종속되면 오히려 복잡도 증가 위험 있음 |


