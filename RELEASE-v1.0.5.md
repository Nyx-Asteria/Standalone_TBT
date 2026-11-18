# Phone Projection TBT 1.0.5

1.0.0 이후의 공통 수정 사항을 모은 일반 릴리즈입니다. 교차로 확대 안내는 계속 선택 가능한 시험 기능입니다.

## 변경 사항

- **AA 카메라·임시 안내 혼입 보완:** 경로와 맞지 않는 짧은 거리가 방향 없는 안내에 섞여도 기존 회전 아이콘·거리를 유지하도록 보완했습니다.
- **교차로 진입점 보완:** 완전히 관측된 작은 교차로에서 내부 연결이 진입 도로로 선택된 경우, 실제 외부 진입 도로와 거리를 다시 맞춥니다.
- **빈 회전교차로 확대 방지:** 도로 가지를 확보하지 못하면 빈 원형 대신 일반 회전교차로 안내를 유지합니다.
- **요금소 상태 재전송:** 이름·거리 막대가 같아도 요금소 상태를 다시 적용하도록 보완했습니다.
- **도착 시간 선택:** 12시간·24시간·차량 시간 설정 중 선택할 수 있습니다.
- **CN7 PE 시험 기능 삭제:** 1.0.4의 계기판 시작 신호 보완 시험 옵션과 관련 코드를 제거했습니다.

## 설치

- **기존 설치 업데이트:** `phone-projection-tbt-1.0.5.zip`을 풀지 않고 USB `HMGAPI/updates/`에 넣고 적용·재시작해 주세요.
- **처음 설치:** `phone-projection-tbt-1.0.5-usb-install.zip`을 풀고, 나온 `NEMESIS.data`와 안쪽 `phone-projection-tbt-1.0.5-nemesis-package.zip`을 USB 최상위에 함께 넣어 주세요. 안쪽 ZIP은 풀지 마세요.
- **NEMESIS 애플리케이션 패키지:** `phone-projection-tbt-1.0.5-nemesis-package.zip`입니다. USB 설치 묶음과 용도가 다릅니다.
- HMG-API 1.0.2 이상, 차량에 맞는 맵과 `Hooks on (map checked)`가 필요합니다.

시간 형식은 TBT 설정 → **도착 예정 시간 표시**에서 선택한 뒤 적용·재시작해 주세요. 앞자리 `0` 및 거리의 `.0` 표시 방식 선택은 지원하지 않습니다.

## 검증

39개 Java·10개 Python 시험, 카메라/방향 없는 안내의 실제 입력 재생, 기존 교차로 입력 재생, D8 변환·서명·ZIP 무결성 검증을 통과했습니다. 호스트 시험과 로그 재생은 실차 화면 검증을 대신하지 않습니다. 순정 엔진·차량 원본 로그·서명 개인키는 배포하지 않습니다.

[호환성](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/blob/main/COMPATIBILITY.md) · [설치](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Installation) · [변경 이력](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/blob/main/CHANGELOG.md)

## English

General release combining the fixes since 1.0.0: isolate conflicting distances in directionless AA notices, correct verified short internal junction approaches, avoid empty rotary enlargement, reapply toll state, select 12/24-hour arrival format, and remove the 1.0.4 CN7 PE start-handshake experiment. Enlargement remains optional and experimental. All 39 Java and 10 Python suites and real-input replays passed; in-vehicle validation remains separate.
