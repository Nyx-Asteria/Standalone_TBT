> 현재 일반 릴리즈: 1.0.5 (2026-11-18). 최신 호환성과 검증 대기 항목은 [[Compatibility]]를 확인해 주세요.


# Troubleshooting — 문제 해결

[Home](Home) · [Installation](Installation) · [Known Issues](Known-Issues)

HMG Phone Projection TBT 설치 및 사용 중 발생할 수 있는 문제와 점검 방법을 안내합니다.

문제가 발생했다면 먼저 차량의 Phone Projection 연결, HMG-API 상태 및 내비게이션 앱 설정을 확인해 주세요.

---

## 1. 증상별 빠른 확인

| 증상 | 먼저 확인할 사항 |
| --- | --- |
| 계기판에 TBT가 표시되지 않음 | HMG-API Hooks, 경로 안내, 계기판 화면 |
| HUD에 안내가 표시되지 않음 | HUD 지원 여부, 앱 HUD 연동 설정 |
| CarPlay가 연결되지 않음 | Phone Projection 연결 모드 |
| 업데이트가 적용되지 않음 | 업데이트 ZIP 경로 및 기존 파일 |
| 다음 안내가 표시되지 않음 | 앱의 다음 안내 데이터 제공 여부 |
| 차선 일부가 누락됨 | 앱별 차선 정보 및 알려진 제한 |
| 특정 심볼이 잘못 표시됨 | 특수 심볼 지원 상태 |
| Korea Projection 패치 충돌 | 중복 TBT 기능 및 네이티브 패치 |
| HMG-API Hooks 오류 | 차량 맵 및 검사 상태 |

---

## 2. 계기판에 TBT가 표시되지 않음

다음 사항을 순서대로 확인하세요.

1. CarPlay 또는 Android Auto가 정상 연결되어 있는지 확인합니다.
2. 내비게이션 앱에서 실제 경로 안내를 시작합니다.
3. 계기판의 내비게이션 안내 화면을 선택합니다.
4. HMG-API에서 `Hooks on (map checked)` 상태를 확인합니다.
5. Phone Projection TBT 설치 및 업데이트 여부를 확인합니다.
6. 다른 모드의 중복 TBT 기능이 활성화되어 있는지 확인합니다.

CarPlay NAVER 지도를 사용하는 경우 앱의 CarPlay HUD 연동 기능도 활성화해 주세요.

위 조건을 충족하는데도 표시되지 않으면 진단 로그를 확인해야 합니다.

---

## 3. HMG-API 맵 또는 Hooks 문제

HMG-API에 차량에 맞는 맵이 없거나 검사가 완료되지 않았다면 관련 훅이 정상 동작하지 않을 수 있습니다.

차량에 맞는 맵이 없다면 다음 기능을 이용합니다.

`Map this car from its files`

생성 후에는 다음 기능을 실행합니다.

`Check the map on this car`

정상적인 목표 상태:

`Hooks on (map checked)`

스팅어 마이스터의 사용자 제보에서는 차량 맵을 직접 생성하고 검사한 뒤 해당 상태를 확인했습니다.

단, 차량마다 맵 검색 항목 수는 다를 수 있습니다.

맵 검사 실패가 계속된다면 임의로 다른 차량의 맵을 적용하지 말고 해당 오류를 기록해 제보해 주세요.

---

## 4. CarPlay 또는 Android Auto 연결 문제

Phone Projection TBT는 무선 연결 기능 자체를 제공하지 않습니다.

따라서 연결이 시작되지 않는 경우에는 TBT 표시 문제와 Phone Projection 연결 문제를 구분해야 합니다.

확인 사항:

- 유선 또는 무선 연결 기능 자체가 정상 작동하는지
- 해당 Phone Projection 모드가 정상적으로 설치되어 있는지
- 연결에 사용하는 차량 및 휴대전화 설정에 문제가 없는지
- 함께 설치된 다른 모드와 충돌이 있는지

Korea Projection과 함께 사용한 스팅어에서 일시적인 무선 CarPlay 연결 실패가 제보되었지만, 이후 동일한 설정에서 정상 연결되었습니다.

해당 현상의 원인은 확인되지 않았습니다.

---

## 5. 다음 안내가 표시되지 않음

현재 안내는 표시되지만 다음 안내가 나오지 않는 경우가 있습니다.

다음 안내는 앱에서 다음 회전과 거리에 대한 데이터를 실제로 제공해야 표시할 수 있습니다.

### CarPlay

v0.1.57 및 v0.1.58에서 초기 안내 처리와 일부 회전 유형의 다음 안내 변환을 개선했습니다.

문제가 반복되면 다음 정보를 기록해 주세요.

- 사용한 지도 앱
- 모드 버전
- 다음 안내가 사라진 상황
- 실제 휴대전화 화면의 안내
- 계기판 표시 결과

### Android Auto

Android Auto의 다음 안내 수신 경로는 구현되어 있으나 앱별 출력 검증이 계속 진행 중입니다.

NAVER 지도와 TMAP의 기본 안내 및 목적지 잔여 거리가 정상 표시되는 것과 별개로 다음 안내 기능은 추가 확인이 필요합니다.

---

## 6. 차선 안내가 부정확함

다음과 같은 증상이 발생할 수 있습니다.

- 추천 차선 색상이 표시되지 않음
- 직좌·직우 복합 화살표의 일부 방향이 누락됨
- 일부 차선이 회색으로 표시됨
- 포켓 차선이 표시되지 않음

앱이 충분한 차선 및 추천 정보를 제공하는지에 따라 표시 결과가 달라집니다.

포켓 차선은 최신 순정 차선 데이터와 Phone Projection 차선의 수·방향이 호환되어야 합니다.

해당 조건을 충족하지 않으면 일부 표현이 제한될 수 있습니다.

---

## 7. 휴게소 또는 특수 심볼이 표시되지 않음

휴게소 단독 숟가락·포크 심볼은 현재 미해결 상태입니다.

시험 결과:

| 코드 | 결과 |
| --- | --- |
| `181` | 단독 심볼 미확인 |
| `182` | 단독 심볼 미확인 |
| `183` | 휴게소 진출로 도식 확인 |

현재로서는 원하는 단독 휴게소 심볼을 모든 차량에서 표시할 수 있는 확정된 설정이 없습니다.

요금소, 터널, 고가도로, 지하차도 등도 앱이 제공하는 안내 데이터에 따라 일반 회전으로 표시될 수 있습니다.

자세한 내용은 [[Known-Issues]]를 참고하세요.

---

## 8. Korea Projection 네이티브 패치 충돌

Korea Projection과 Phone Projection TBT를 함께 사용하는 환경에서 Android Auto 네이티브 패치 충돌이 보고되었습니다.

보고된 메시지:

```text
native patch libautoreceiver_infobank.so not applied
aa-tbt-48ee793a: conflict with wired-carplay-tbt-lx2pe's aa-tbt-48ee793a
```

### 권장 설정

Korea Projection의 다음 옵션을 비활성화합니다.

`Turn-by-turn on the cluster and HUD`

Phone Projection TBT만 계기판 TBT를 담당하도록 설정합니다.

다만 이 설정은 중복 TBT 출력을 방지하기 위한 것으로, Android Auto 네이티브 패치 충돌 자체를 해결하지는 않습니다.

충돌 로그가 지속된다면 다음 정보를 함께 제보해 주세요.

- Korea Projection 버전
- Phone Projection TBT 버전
- HMG-API 버전
- 관련 네이티브 패치 오류 로그

검증되지 않은 라이브러리 수동 교체나 패치 강제 적용은 권장하지 않습니다.

---

## 9. 업데이트가 적용되지 않음

다음 사항을 확인합니다.

- 올바른 업데이트 ZIP을 사용했는지
- ZIP을 압축 해제하지 않았는지
- USB의 `HMGAPI/updates/` 경로가 올바른지
- 이전 TBT 업데이트 ZIP이 함께 남아 있지 않은지
- `Check USB updates`를 실행했는지
- `Apply changes` 후 AVN을 재시작했는지

최초 설치용 NEMESIS 패키지와 기존 설치용 업데이트 ZIP은 용도가 다릅니다.

상세한 절차는 [[Installation]]을 참고하세요.

---

## 10. 해결되지 않는 경우

진단 로그를 수집하고 GitHub Issue로 제보해 주세요.

필요한 정보:

- 차량 및 AVN 펌웨어
- HMG-API 및 TBT 버전
- 내비게이션 앱
- 유선 또는 무선 연결
- 발생한 증상
- 재현 단계
- 발생 시각
- 개인정보를 가린 로그

로그 수집 방법은 [[Logs-and-Issue-Reporting]]을 참고하세요.

[GitHub Issues](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/issues)
