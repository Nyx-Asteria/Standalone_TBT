# HMG Phone Projection TBT 1.0.0

[English](README.en.md) · [호환성](COMPATIBILITY.md) · [Wiki](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki) · [릴리즈](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/releases/latest)

Hyundai/Kia Gen5W에서 CarPlay·Android Auto 내비게이션 안내를 계기판과 HUD로 전달하는 HMG-API 모드입니다. 현재·다음 방향, 거리, 도로명, 목적지 정보와 앱이 제공하는 차선 정보를 처리합니다. 순정 지도 정보를 이용하는 교차로 확대 안내는 설정에서 켤 수 있는 시험 기능입니다.

## 설치와 업데이트

HMG-API 1.0.2 이상 및 차량에 맞는 맵을 설치하고 `Hooks on (map checked)`를 확인해 주세요.

- 처음 설치: 릴리즈의 `phone-projection-tbt-1.0.0-usb-install.zip`을 풀고, `NEMESIS.data`와 안쪽 ZIP을 USB 최상위에 함께 넣어 주세요. 안쪽 ZIP은 풀지 마세요.
- 기존 설치 업데이트: `phone-projection-tbt-1.0.0.zip`을 압축 해제하지 않고 USB의 `HMGAPI/updates/`에 넣어 주세요.
- 적용 후 헤드유닛을 재시작해 주세요. Korea Projection을 함께 쓰는 경우 중복 TBT 전송 옵션은 꺼 주세요.

자세한 절차는 [설치 안내](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Installation)를 참고해 주세요.

## 1.0.0 변경 사항

CarPlay 안내 구독 중단 복구, 별도 로더에서 발생한 확대 안내 충돌 방어, 안내 번호·도로명 교체 시 확대 깜빡임 억제, AA 진출입로와 복수 안내 문구 처리를 포함합니다. AA 좌측 갈림길은 10시 방향으로 복원했습니다. 요금소명·거리 막대 유지, 진출로 경로, 회전교차로 연결 정보 처리와 24시간 ETA 요청도 반영했습니다.

[릴리즈 설명](RELEASE-v1.0.0.md)과 [호환성·남은 확인 항목](COMPATIBILITY.md)을 확인해 주세요. 일반 릴리즈 전환은 모든 차종의 확대 안내 검증 완료를 의미하지 않습니다. 최신 요금소·진출로·회전교차로 수정은 실차 재검증이 필요합니다.

## 소스와 라이선스

[빌드 방법](BUILD.md) · [변경 이력](CHANGELOG.md) · [외부 코드 고지](NOTICE.txt). AGPL-3.0으로 배포합니다. 차량 로그·순정 지도 엔진·펌웨어·서명 개인키는 포함하지 않습니다. 배포된 JNI 보조 라이브러리의 소스는 `navigation-api/native`에 있습니다.
