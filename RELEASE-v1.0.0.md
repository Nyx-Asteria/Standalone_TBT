# Phone Projection TBT 1.0.0

일반 릴리즈입니다. 0.1.97의 수정 사항을 포함하고 표시 버전을 1.0.0으로 통일했습니다.

- CarPlay 구독 중단 복구와 확대 안내 로더 오류 방어를 추가했습니다.
- 도로명·안내 번호 변경에 따른 확대 전환과 문구 갱신 지연을 줄였습니다.
- AA 진출입로 분류를 보완하고 좌측 갈림길을 10시 방향으로 복원했습니다.
- 요금소명 재전송·확대 유지, 진출로 경로 각도, 회전교차로 연결 정보 처리를 수정했습니다.
- 24시간 ETA 형식을 요청하고 10km 이상 거리를 정수값으로 전달합니다. 소수점 표시 형식은 차량 UI에 따라 다릅니다.
- LX2 PE 및 수집된 DN8 HEV·K5 DL3·CK PE 엔진의 두 검증 해시를 허용합니다. 미등록 엔진의 지도 조회는 생략합니다.

## 설치

처음 설치하실 때는 `phone-projection-tbt-1.0.0-usb-install.zip`, 기존 설치를 업데이트하실 때는 `phone-projection-tbt-1.0.0.zip`을 사용해 주세요. HMG-API 1.0.2 이상과 차량에 맞는 맵이 필요합니다. 업데이트 ZIP은 풀지 않고 HMGAPI/updates/에 넣어 주세요.

## 검증 범위

37개 Java 시험과 10개 Python 회귀 시험, D8 변환, 패키지 서명 및 ZIP 무결성을 확인했습니다. 최신 요금소명·깜빡임, 진출로 파란 경로, 회전교차로 가지의 실차 재검증은 남아 있습니다. 확대 안내는 설정에서 켜는 시험 기능이며 일반 릴리즈 전환이 모든 차량의 검증 완료를 의미하지 않습니다.

[호환성](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/blob/main/COMPATIBILITY.md) · [설치](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Installation)

## English

General release based on the latest 0.1.97 changes, with version markers unified to 1.0.0. Includes CarPlay subscription recovery and enlargement error containment, stable maneuver tracking, AA ramp handling and restored 10-o'clock left-fork output, toll-name/hold improvements, factory off-ramp geometry, roundabout graph handling and 24-hour ETA requests. Host checks do not replace vehicle testing: the latest toll, ramp and roundabout fixes need driving validation. Enlargement remains optional and experimental.
