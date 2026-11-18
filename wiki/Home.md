# HMG Phone Projection TBT 1.0.5

CarPlay·Android Auto 내비게이션 안내를 Hyundai/Kia Gen5W 계기판과 HUD로 전달합니다. [1.0.5 일반 릴리즈](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/releases/tag/v1.0.5) · [소스](https://github.com/ch0iiih/HMG_Phone_Projection_TBT)

- [[Installation]]: 처음 설치와 기존 설치 업데이트
- [[Features]]: 기본 TBT·확대 안내·설정
- [[Compatibility]]: 차량·앱별 작동 확인 범위
- [[Known-Issues]]: 표시 형식과 확대 안내의 제한
- [[Troubleshooting]]: 표시되지 않을 때 확인 사항
- [[Logs-and-Issue-Reporting]]: 로그 수집과 제보
- [[Development]]: 소스 빌드와 검증

## 이번 변경

- **AA 카메라·임시 안내 혼입 보완:** 경로와 맞지 않는 짧은 거리가 방향 없는 안내에 섞여도 기존 회전 아이콘·거리를 유지하도록 보완했습니다.
- **교차로 진입점 보완:** 완전히 관측된 작은 교차로에서 내부 연결이 진입 도로로 선택된 경우, 실제 외부 진입 도로와 거리를 다시 맞춥니다.
- **빈 회전교차로 확대 방지:** 도로 가지를 확보하지 못하면 빈 원형 대신 일반 회전교차로 안내를 유지합니다.
- **요금소 상태 재전송:** 이름·거리 막대가 같아도 요금소 상태를 다시 적용하도록 보완했습니다.
- **도착 시간 선택:** 12시간·24시간·차량 시간 설정 중 선택할 수 있습니다.
- **CN7 PE 시험 기능 삭제:** 1.0.4의 계기판 시작 신호 보완 시험 옵션과 관련 코드를 제거했습니다.

확대 안내는 선택 가능한 시험 기능입니다. 버전별 검증 범위는 [변경 이력](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/blob/main/CHANGELOG.md)을 확인해 주세요.
