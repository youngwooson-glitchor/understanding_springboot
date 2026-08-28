# BUG_FIX

## 2026-08-27 OAuth2 로그인 관련 컴파일 오류 수정

### 문제 원인

`config/oauth/` 패키지의 OAuth2 로그인 성공 처리 및 쿠키 기반 authorization request 저장소 구현에 컴파일을 막는 오타·타입 불일치가 3건 존재했음.

- `OAuth2SuccessHandler.java:46` — `getTargetUrl(acessToken)` 호출 시 `accessToken`을 `acessToken`으로 오타. 해당 이름의 변수가 없어 컴파일 불가.
- `OAuth2SuccessHandler.java:76` — `clearAuthenticationAttributes`에서 `authorizationRequestRepository` 필드를 참조했으나, 클래스에 선언된 필드명은 `auth2AuthorizationRequestBasedOnCookieRepository`(생성자 주입, `@RequiredArgsConstructor`)였음. 존재하지 않는 필드 참조로 컴파일 불가.
- `OAuth2AuthorizationRequestBasedOnCookieRepository.java:26,28` — `Cookie` 타입을 `org.springframework.boot.web.server.Cookie`로 import했으나, 실제 사용하는 `WebUtils.getCookie(...)`와 `CookieUtil.deserialize(...)`는 모두 `jakarta.servlet.http.Cookie`를 기대함. 서로 다른 `Cookie` 타입 간 타입 불일치로 컴파일 불가.

재현 조건: 세 파일 모두 `git status`상 신규 작성/미커밋 상태였으며, `./gradlew compileJava` 실행 시 즉시 재현됨.

### 해결 방안

- `acessToken` → `accessToken`으로 오타 수정 (변수명 자체는 `onAuthenticationSuccess` 내에 이미 올바르게 선언되어 있었으므로, 호출부만 정정).
- `authorizationRequestRepository` → 실제 필드명인 `auth2AuthorizationRequestBasedOnCookieRepository`로 정정. 필드를 새로 추가하거나 이름을 바꾸는 대신 호출부를 기존 필드명에 맞춤 — 필드명 변경 시 생성자 주입 방식(`@RequiredArgsConstructor`) 특성상 다른 호출부에도 영향이 번질 수 있어 배제.
- `OAuth2AuthorizationRequestBasedOnCookieRepository.java`의 import를 `org.springframework.boot.web.server.Cookie`에서 `jakarta.servlet.http.Cookie`로 교체. `CookieUtil`이 이미 `jakarta.servlet.http.Cookie`를 표준으로 사용 중이므로 이에 맞춤 — `CookieUtil` 쪽을 변경하는 대신 잘못 import된 쪽을 수정하는 것이 영향 범위가 작음.

### 결과

`./gradlew compileJava` 실행 결과 `BUILD SUCCESSFUL`로 컴파일 통과 확인. 남은 경고는 `CookieUtil.java`의 기존 deprecated API 사용에 대한 것으로, 이번 수정 대상과 무관함.

## 2026-08-27 `./gradlew build` 실패 — OAuth2 설정 누락 및 테스트 코드 결함

### 문제 원인

컴파일 수정 후에도 `./gradlew build`가 계속 실패했음. 원인은 두 단계로 나뉨.

**1) ApplicationContext 로딩 실패로 테스트 12개 전부 실패**
- `application.yml`에 Google OAuth2 클라이언트 설정이 `security.oauth2.client.registration...` 경로(최상위 `security:` 블록)로 들어가 있었음. Spring Boot의 OAuth2 client 자동설정이 실제로 읽는 프로퍼티 경로는 `spring.security.oauth2.client.registration...`이므로, Boot가 이 값을 전혀 인식하지 못함.
- 그 결과 `ClientRegistrationRepository` 빈이 자동 생성되지 않았고, `WebOAuthSecurityConfig.filterChain()`의 `.oauth2Login(...)` 설정이 이 빈을 요구하면서 `NoSuchBeanDefinitionException` → `filterChain` 빈 생성 실패 → `ApplicationContext` 전체 로딩 실패로 이어져, 컨텍스트를 띄우는 테스트 전부가 연쇄 실패함.

**2) 설정 수정 후에도 남은 테스트 6건 실패**
- `BlogApiControllerTest`(5건): `Article.author` 컬럼이 `nullable = false`인데 테스트 픽스처의 `Article.builder()`에 `.author(...)`가 빠져 있어 저장 시 `DataIntegrityViolationException` 발생. 또한 `addArticle` 테스트는 인증 정보 없이 요청을 보내는데 `BlogApiController.addArticle`이 `Principal.getName()`을 호출해 NPE, `deleteArticle`/`updateArticle`은 `BlogService.authorizeArticleAuthor`가 `SecurityContextHolder`의 인증 정보를 요구해 같은 이유로 NPE.
- `TokenProviderTest`(1건): `TokenApiControllerTest`가 동일 이메일(`user@gmail.com`)로 유저를 저장한 뒤 롤백 없이 커밋하고, `TokenProviderTest`는 정리 로직이 없어 같은 이메일로 저장을 시도하면서 `EMAIL` 유니크 제약 충돌(`DataIntegrityViolationException`) 발생. `@BeforeEach`에 `deleteAll()`만 추가했을 때도 계속 실패했는데, 이는 `User`의 PK 생성 전략이 `GenerationType.IDENTITY`라서 `save()` 시 INSERT가 즉시 실행되는 반면 `deleteAll()`의 DELETE는 다음 flush까지 지연되어, 이전 커밋된 행이 물리적으로 삭제되기 전에 INSERT가 먼저 실행되어 충돌했기 때문.

재현 조건: `git status`상 미커밋 상태였던 `application.yml`, `WebOAuthSecurityConfig.java`와 신규 테스트 코드 조합에서 `./gradlew build` 실행 시 항상 재현됨.

### 해결 방안

- `application.yml`: `security:` 블록을 `spring:` 블록 하위로 이동해 `spring.security.oauth2.client.registration.google...` 경로로 정정 (값 자체는 변경 없음).
- `BlogApiControllerTest`: 모든 `Article.builder()` 픽스처에 `.author("test@gmail.com")` 추가. `addArticle` 테스트는 요청에 `.principal(new UsernamePasswordAuthenticationToken("test@gmail.com", "password"))`를 추가해 `Principal`을 주입. `deleteArticle`/`updateArticle`은 `@WithMockUser(username = "test@gmail.com")`를 붙여 `SecurityContextHolder`에 인증 정보가 채워지도록 함 — 두 인증 방식이 다른 이유는 컨트롤러 계층은 `Principal` 파라미터를, 서비스 계층은 `SecurityContextHolder`를 각각 참조하기 때문.
- `TokenProviderTest`: `@Transactional`을 추가해 테스트 간 데이터가 롤백되도록 하고, `@BeforeEach`에서 `userRepository.deleteAll()` 후 `userRepository.flush()`를 호출해 IDENTITY 전략의 즉시 INSERT보다 DELETE가 먼저 실제 반영되도록 함. `TokenApiControllerTest`를 함께 고치는 대신 `TokenProviderTest`만 수정한 이유: 실패의 표면 증상이 `TokenProviderTest`에서 발생했고, `TokenApiControllerTest`는 이미 자체 `@BeforeEach`에서 `deleteAll()` 정리 패턴을 갖고 있어 동일 패턴을 이쪽에도 맞춘 것이 최소 변경.

### 결과

`./gradlew build --rerun-tasks` 실행 결과 `BUILD SUCCESSFUL` 확인 (테스트 12개 전원 통과). 프로덕션 코드는 `application.yml` 1개 파일만 수정했고, 나머지는 모두 테스트 코드(`BlogApiControllerTest.java`, `TokenProviderTest.java`) 변경임.
