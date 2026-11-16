# 설치 및 업데이트 — 1.0.0

[최신 일반 릴리즈](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/releases/latest) · [[Compatibility]]

## 요구 사항

Gen5W에서 작동하는 HMG-API 1.0.2 이상, 차량에 맞는 맵, `Hooks on (map checked)`와 정상 작동하는 CarPlay 또는 Android Auto 연결이 필요합니다. 이 모드는 무선 연결 자체를 활성화하지 않습니다.

## 처음 설치하시는 경우

1. `phone-projection-tbt-1.0.0-usb-install.zip`을 다운로드하고 압축을 풀어 주세요.
2. 나온 `NEMESIS.data`와 `phone-projection-tbt-1.0.0-nemesis-package.zip`을 USB 최상위에 함께 넣어 주세요.
3. 안쪽 `nemesis-package.zip`은 압축 해제하지 마세요. 안쪽의 type=application 설명 파일을 USB 루트에 따로 넣으면 안 됩니다.
4. USB를 차량에 연결하고 NEMESIS 설치 확인창에서 설치해 주세요.
5. 설치 완료 후 헤드유닛을 재시작해 주세요.

## 이미 설치하신 경우

1. `phone-projection-tbt-1.0.0.zip`을 압축 해제하지 않고 USB `HMGAPI/updates/`에 넣어 주세요.
2. HMG-API에서 업데이트를 적용하고 헤드유닛을 재시작해 주세요.

## 설정

Korea Projection을 함께 사용하는 경우 중복 TBT 전송 옵션은 꺼 주세요. 확대 안내는 TBT 설정에서 별도로 켜는 시험 기능입니다. 설정을 적용한 뒤 재시작해 주세요. 미등록 지도 엔진에서는 지도 조회를 생략하고 기본 TBT를 유지합니다.

소스는 `phone-projection-tbt-1.0.0-source.zip`, 파일 무결성 확인값은 `phone-projection-tbt-1.0.0-SHA256SUMS.txt`를 참고해 주세요. 세부 호환성과 최신 수정의 실차 검증 대기 항목은 [[Compatibility]]에 정리되어 있습니다.
