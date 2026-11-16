> 현재 일반 릴리즈: 1.0.0 (2026-11-17). 최신 호환성과 검증 대기 항목은 [[Compatibility]]를 확인해 주세요.


# Installation — 설치 및 업데이트

[Home](Home) · [Compatibility](Compatibility) · [Troubleshooting](Troubleshooting)

HMG Phone Projection TBT의 설치, 업데이트 및 기본 설정 방법을 안내합니다.

**현재 공개 버전:** `v0.1.58` (Prerelease)

[최신 릴리스 및 설치 파일](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/releases/latest)

> **주의:** 모든 설치 및 업데이트 작업은 차량을 안전한 장소에 정차한 상태에서 진행해 주세요. 이 모드는 비공식 소프트웨어이며, 모든 차량과 펌웨어에서의 호환성이 보장되지는 않습니다.

---

## 1. 설치 전 확인 사항

| 항목 | 요구 사항 |
| --- | --- |
| AVN | HMG-API가 동작하는 지원 환경 |
| HMG-API | 1.0.2 이상 |
| HMG-API 맵 | 차량에 적합한 맵 필요 |
| Hooks 상태 | `Hooks on (map checked)` |
| Phone Projection | 유선 또는 무선 CarPlay / Android Auto 연결 |
| 설치 도구 | NEMESIS |

이 모드는 **무선 CarPlay 또는 Android Auto 자체를 활성화하지 않습니다.**

차량에서 Phone Projection 연결이 먼저 정상적으로 작동해야 합니다.

차종 및 펌웨어별 시험 결과는 [[Compatibility]]를 참고하세요.

---

## 2. 설치 파일 종류

v0.1.58은 다음 세 가지 파일로 배포됩니다.

| 파일 | 용도 |
| --- | --- |
| `phone-projection-tbt-0.1.58-nemesis-package.zip` | 최초 설치 |
| `phone-projection-tbt-0.1.58.zip` | 기존 설치 업데이트 |
| `phone-projection-tbt-0.1.58-source.zip` | 소스 코드 및 개발 자료 |

**최초 설치 패키지와 업데이트 패키지를 혼동하지 마세요.**

파일은 [GitHub Releases](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/releases/tag/v0.1.58)에서 확인할 수 있습니다.

---

## 3. 최초 설치

### Step 1. HMG-API 준비

먼저 HMG-API 1.0.2 이상을 설치합니다.

[HMG-API 프로젝트](https://github.com/VysseCodes/HMG-API)

차량에 맞는 HMG-API 맵이 없다면 다음 기능으로 직접 생성할 수 있습니다.

`Map this car from its files`

생성 후에는 다음 기능으로 맵을 검사합니다.

`Check the map on this car`

검사가 완료되면 필요에 따라 AVN을 재시작합니다.

### Step 2. Hooks 상태 확인

HMG-API에서 다음 상태가 표시되는지 확인합니다.

`Hooks on (map checked)`

이 상태가 확인되지 않았다면 TBT 모드를 설치하기 전에 HMG-API 맵과 훅 설정을 점검하세요.

### Step 3. Phone Projection TBT 설치

NEMESIS에서 다음 최초 설치 패키지를 사용합니다.

`phone-projection-tbt-0.1.58-nemesis-package.zip`

NEMESIS의 패키지 설치 기능을 통해 설치한 뒤 AVN을 재시작합니다.

### Step 4. 기능 확인

1. CarPlay 또는 Android Auto를 연결합니다.
2. 지원하는 내비게이션 앱에서 경로 안내를 시작합니다.
3. 차량 계기판에서 내비게이션 안내 화면을 선택합니다.
4. TBT 안내가 표시되는지 확인합니다.

CarPlay NAVER 지도에서는 앱의 CarPlay HUD 연동 설정도 활성화해 주세요.

앱별 지원 상태는 [[Compatibility]]를 참고하세요.

---

## 4. 기존 버전 업데이트

이미 Phone Projection TBT가 설치되어 있다면 HMG-API 업데이트 기능을 이용할 수 있습니다.

### Step 1. 업데이트 파일 준비

업데이트 ZIP을 USB의 다음 경로에 복사합니다.

`HMGAPI/updates/`

예시:

```text
USB/
└── HMGAPI/
    └── updates/
        └── phone-projection-tbt-0.1.58.zip
```

**ZIP 파일은 압축을 해제하지 마세요.**

이전에 복사해 둔 Phone Projection TBT 업데이트 ZIP이 있다면 제거합니다.

### Step 2. HMG-API 업데이트 실행

차량을 정차한 상태에서 HMG-API를 실행하고 다음 순서로 진행합니다.

1. `Check USB updates`
2. `Apply changes`
3. `Restart Unit`

AVN이 재시작된 뒤 TBT 기능을 확인합니다.

기존 모드 ID와 서명자는 업데이트 호환성을 유지하도록 설계되어 있습니다.

---

## 5. 다른 Phone Projection 모드와 함께 사용

Korea Projection 등 다른 Phone Projection 모드에서 계기판 TBT 기능을 제공하는 경우, 두 모드의 TBT 기능을 동시에 활성화하지 않는 것을 권장합니다.

프로젝션 모드의 다음과 같은 옵션을 비활성화해 주세요.

`Turn-by-turn on the cluster and HUD`

Phone Projection TBT가 계기판 TBT를 담당하도록 설정합니다.

> **알려진 문제:** Korea Projection과 Phone Projection TBT 사이에서 Android Auto 관련 네이티브 패치 충돌이 보고되었습니다. 단순히 TBT 옵션을 끄는 것으로 네이티브 패치 충돌까지 해결되는 것은 아닙니다.

자세한 내용은 [[Known-Issues]] 및 [[Troubleshooting]]을 참고하세요.

---

## 6. 제거 및 이전 버전 복원

현재 공개 프로젝트 문서에는 **모든 설치 환경에 공통적으로 적용할 수 있는 검증된 제거 및 롤백 절차가 명시되어 있지 않습니다.**

따라서 임의로 AVN 내부 파일이나 시스템 라이브러리를 삭제하는 방법은 권장하지 않습니다.

제거 또는 이전 버전 복원이 필요하다면 다음 정보를 먼저 확인하세요.

- 설치에 사용한 NEMESIS 및 HMG-API 버전
- 설치된 Phone Projection TBT 버전
- 함께 설치된 다른 Phone Projection 모드
- 현재 HMG-API Hooks 상태
- 제거 또는 복원이 필요한 이유

기존 설치가 비정상적으로 작동한다면 관련 로그를 확보한 뒤 GitHub Issue를 통해 해당 환경의 적절한 복구 절차를 확인해 주세요.

[GitHub Issues](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/issues)

---

## 7. 설치 후 문제가 발생한다면

| 증상 | 확인할 사항 |
| --- | --- |
| 모드가 작동하지 않음 | HMG-API Hooks 및 맵 상태 |
| 계기판 안내가 나타나지 않음 | 경로 안내 활성화, 앱 설정, 계기판 화면 |
| 무선 CarPlay 연결 실패 | 무선 연결 모드 자체의 동작 상태 |
| Korea Projection 충돌 | 중복 TBT 설정 및 네이티브 패치 오류 |
| 특정 심볼만 표시되지 않음 | 앱별 지원 상태 및 알려진 제한 |

상세한 해결 방법은 [[Troubleshooting]]을 참고하세요.

---

## 관련 문서

- [[Compatibility]] — 차량 및 앱 호환성
- [[Features]] — 지원 기능
- [[Known-Issues]] — 알려진 문제
- [[Troubleshooting]] — 문제 해결
- [[Logs-and-Issue-Reporting]] — 진단 로그 수집

*문서 기준: v0.1.58 / 2026-11-14.*
