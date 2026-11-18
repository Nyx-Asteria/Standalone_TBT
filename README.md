# HMG Phone Projection TBT 1.0.5

[English](README.en.md) · [다운로드](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/releases/tag/v1.0.5) · [호환성](COMPATIBILITY.md) · [Wiki](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki)

Hyundai/Kia Gen5W에서 CarPlay·Android Auto 내비게이션 안내를 계기판과 HUD로 전달하는 HMG-API 모드입니다. 현재·다음 방향, 거리, 도로명, 목적지와 앱이 제공하는 차선 안내를 처리합니다. 순정 지도 정보를 이용하는 교차로 확대 안내는 선택 가능한 시험 기능입니다.

## 1.0.5 변경 사항

- **AA 카메라·임시 안내 혼입 보완:** 경로와 맞지 않는 짧은 거리가 방향 없는 안내에 섞여도 기존 회전 아이콘·거리를 유지하도록 보완했습니다.
- **교차로 진입점 보완:** 완전히 관측된 작은 교차로에서 내부 연결이 진입 도로로 선택된 경우, 실제 외부 진입 도로와 거리를 다시 맞춥니다.
- **빈 회전교차로 확대 방지:** 도로 가지를 확보하지 못하면 빈 원형 대신 일반 회전교차로 안내를 유지합니다.
- **요금소 상태 재전송:** 이름·거리 막대가 같아도 요금소 상태를 다시 적용하도록 보완했습니다.
- **도착 시간 선택:** 12시간·24시간·차량 시간 설정 중 선택할 수 있습니다.
- **CN7 PE 시험 기능 삭제:** 1.0.4의 계기판 시작 신호 보완 시험 옵션과 관련 코드를 제거했습니다.

[전체 변경 이력](CHANGELOG.md) · [1.0.5 릴리즈 설명](RELEASE-v1.0.5.md)

## 설치와 업데이트

HMG-API 1.0.2 이상 및 차량에 맞는 맵이 필요합니다. `Hooks on (map checked)`를 확인해 주세요.

| 용도 | 파일 및 방법 |
| --- | --- |
| 기존 설치 업데이트 | `phone-projection-tbt-1.0.5.zip`을 풀지 않고 USB `HMGAPI/updates/`에 넣어 주세요. |
| 처음 설치 | `phone-projection-tbt-1.0.5-usb-install.zip`을 풀고, `NEMESIS.data`와 안쪽 애플리케이션 ZIP을 USB 최상위에 함께 넣어 주세요. 안쪽 ZIP은 풀지 마세요. |
| NEMESIS 애플리케이션 패키지 | `phone-projection-tbt-1.0.5-nemesis-package.zip` |
| 공개 소스 / 무결성 확인 | `phone-projection-tbt-1.0.5-source.zip` / `phone-projection-tbt-1.0.5-SHA256SUMS.txt` |

[릴리즈 다운로드](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/releases/tag/v1.0.5)에서 받아 적용·재시작해 주세요. Korea Projection 등을 함께 사용하면 중복 TBT 전송 기능은 꺼 주세요. [자세한 설치 안내](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Installation)

## 설정

- **Android Auto 길안내:** AA TBT 사용 여부를 선택합니다.
- **CarPlay / Android Auto 확대 안내 · 시험:** 지도 형상과 현재 안내가 대응하는 구간에서 확대를 표시합니다.
- **확대 안내 시작 거리:** 일반 도로400m / 고속도로 진출입1050m 또는 속도별 기준을 선택합니다. 이 값은 상한이며, 형상이 확인되지 않은 구간에서는 일반 안내를 유지합니다.
- **요금소 접근 표시 · 시험:** 요금소로 식별된 안내의 표시 상태와 거리 막대를 처리합니다.
- **도착 예정 시간 표시:** 12시간·24시간·차량 설정 중 선택합니다. 적용·재시작이 필요합니다.

`H:MM/HH:MM`의 앞자리 0과 `90.0km`의 `.0` 제거는 현재 지원하지 않습니다. 10km 이상은 정수 수치를 보내지만 차량이 소수점 한 자리를 표시할 수 있습니다.

## 호환성과 검증

[차량별 확인 범위](COMPATIBILITY.md)에 사용자 확인과 남은 검증 항목을 구분했습니다. K8 GL3 HEV의 AA 계기판 확대·HUD 표시 제보를 추가했습니다. 일반 릴리즈가 모든 차종·앱·확대 도형의 검증 완료를 뜻하지는 않습니다.

1.0.5는 39개 Java·10개 Python 시험과 실제 입력 재생, D8·서명·ZIP 검증을 통과했습니다. 실차 재검증은 별도입니다.

## 로그와 소스

문제 재현 전 USB 최상위에 `TBT-LOGS` 폴더를 만들고 차량에 연결해 주세요. 재현 뒤 생성된 **폴더 전체**를 수집하고, 앱·연결 방식·발생 시각을 함께 기록해 주세요. [로그 안내](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Logs-and-Issue-Reporting)

[빌드 방법](BUILD.md) · [외부 코드 고지](NOTICE.txt). AGPL-3.0으로 배포합니다. 순정 엔진·차량 원본 로그·펌웨어·서명 개인키는 포함하지 않습니다. 프로젝트 JNI 보조 라이브러리 소스는 `navigation-api/native`에 있습니다.
