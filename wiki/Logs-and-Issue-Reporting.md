# 로그 수집 및 문제 제보

## 수집 방법

1. USB 저장장치 최상위에 `TBT-LOGS` 폴더를 만들어 주세요.
2. TBT가 실행되는 차량에 USB를 연결하고 증상을 재현해 주세요.
3. 로그 저장이 완료된 뒤 `TBT-LOGS` **폴더 전체**를 압축해 주세요. 입력·출력을 비교해야 하므로 일부 파일만 고르지 마세요.
4. 차종·연식·전체 펌웨어·TBT 버전·내비 앱·CarPlay/AA·유선/무선·발생 시각을 함께 알려 주세요.

## 주요 파일

| 파일 | 내용 |
| --- | --- |
| `phone-projection-tbt.log`, `stock-guidance.log` | 설치 버전·수집 정보와 일반 기록 |
| `export-status.txt` | 수집 진행·완료·보조 파일 오류 상태 |
| `carplay-input.log`, `carplay-tbt-output.log` | CarPlay 수신 및 차량 API 출력 |
| `aa-input.log`, `aa-input.log.1` | AA 안내·거리·상태 입력 |
| `aa-raw-packets.log` 및 `.1` | AA 원본 알림 패킷 |
| `aa-tbt-output.log` | AA 최종 TBT 출력 |
| `stock-cluster-input.log`, `stock-tbt-output.log` | 순정 표시·경고·TBT 출력 |
| `aa-junction-match.log`, `junction-match.log`, `stock-road-graph.log` | 확대 대상 대응과 도로 형상 진단 |
| `api-result.txt` | 순정 도로 API 준비 단계 |

사용한 연결 방식·안내 여부에 따라 없는 파일이 있을 수 있습니다. 버전·부팅·시각이 다른 과거 행이 섞일 수 있어 파일 하나만으로 현재 상태를 단정하지 않습니다. `finished=1`만으로 안내 데이터까지 수신됐다는 뜻은 아닙니다.

로그에는 실제 도로명·위치·이동 경로가 포함될 수 있습니다. 공개 전 내용을 확인하거나 비공개로 전달해 주세요. `cluster-reference`의 차량 프레임워크 바이너리와 순정 엔진은 공개 이슈에 올리지 마세요.

[이슈 작성](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/issues/new/choose) · [[Troubleshooting]]
