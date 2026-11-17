# Changelog / 변경 내역

## 1.0.0 — 2026-11-17

일반 릴리즈입니다. 0.1.97의 수정 사항을 포함하고 표시 버전을 1.0.0으로 통일했습니다.

- CarPlay 구독 중단 복구와 확대 안내 로더 오류 방어를 추가했습니다.
- 도로명·안내 번호 변경에 따른 확대 전환과 문구 갱신 지연을 줄였습니다.
- AA 진출입로 분류를 보완하고 좌측 갈림길을 10시 방향으로 복원했습니다.
- 요금소명 재전송·확대 유지, 진출로 경로 각도, 회전교차로 연결 정보 처리를 수정했습니다.
- 24시간 ETA 형식을 요청하고 10km 이상 거리를 정수값으로 전달합니다. 소수점 표시 형식은 차량 UI에 따라 다릅니다.
- LX2 PE 및 수집된 DN8 HEV·K5 DL3·CK PE 엔진의 두 검증 해시를 허용합니다. 미등록 엔진의 지도 조회는 생략합니다.



> 0.1.59~0.1.97은 개발·시험 빌드의 이력입니다. 모든 번호가 GitHub 공개 릴리즈였던 것은 아니며, 적용 보류 항목과 후속 변경을 함께 기록했습니다. 각 버전의 구현·시험 결과는 해당 기능의 전체 차량 검증 완료를 뜻하지 않습니다.

## 0.1.97

- AA 좌측 갈림길을 10시 방향(71/330도), 우측 갈림길을 66/30도로 복원했습니다. 한국형 진출입로 선택자는 유지했습니다.
- ETA를 24시간 형식으로 요청하고 10km 이상 거리를 정수값으로 전달합니다. `.0` 표시 여부는 계기판 UI에 따릅니다.
- 고속도로 진출 확대를 순정 고정 도식의 우측17/90도·좌측18/270도로 수정했습니다.
- 회전교차로의 실제 원형 연결과 진입·출구까지의 경로 거리를 검증하도록 보완했습니다. 불완전한 가지는 만들지 않습니다.
- 요금소 확대의 거리 흔들림 허용, 1초 상태 재전송, 표시 상태 적용 이후 이름 재전송을 추가했습니다.
- AA 안내 종류·방향 변경 때 이전 안내 거리를 새 확대 화면에 사용하지 않도록 했습니다. 최신 화면 동작은 실차 재검증 대상입니다.

## 0.1.96

- Korea Projection의 한국용 소스와 비교해 AA 진입160~162, 진출163~165 선택을 반영했습니다. 이 버전의 갈림길173/174 선택은 0.1.97에서 복원했습니다.
- 같은 회전에서 지명만 바뀌면 3.5초 대기를 거치지 않고 즉시 갱신하도록 했습니다.
- 요금소명↔금액, 휴게소명↔유가 형태의 교대 문구에서 확인된 시설 분류를 제한된 시간 동안 유지합니다. 수신하지 않은 가격은 생성하지 않습니다.
- 실제 요금소명/금액 입력 재생으로 아이콘 유지 여부를 검사했습니다. 계기판 시현과 복수 문구 동기화는 추가 실차 확인이 필요합니다.

## 0.1.95

- 도로명·폰 안내 항목 번호가 바뀌어도 회전 종류·방향과 거리 흐름이 같으면 확대 대상을 유지하도록 했습니다.
- 안내 번호나 교대 지명 때문에 형상 조회와 확대 확인이 반복 초기화되는 경로를 보완했습니다.
- 검증된 물리 도로 가지를 사용하는 확대 프레임 조합을 정리했습니다. 진출 고정 도식은 이후 0.1.97에서 다시 보완했습니다.
- 요금소 이름을 표시 프레임 뒤에 전송하고, 폰 안내 소유 중 순정 교차로명이 덮어쓰는 경로를 제한했습니다.

## 0.1.94

- 순정 요금소 처리와 대조해 요금소 아이콘·막대 외에 별도 `displayTG` 표시 상태를 반영했습니다.
- 실제로 수신한 순정 단속 프레임의 사본에서 요금소 표시 상태와 막대만 바꿉니다. 카메라·제한속도·통행료 등 원본 필드는 보존합니다.
- 종료·연결 해제·만료·요금소 이탈 시 각 API의 순정 값을 복원합니다. 실차 요금소 확대 결과는 별도 확인 대상입니다.

## 0.1.93

- CarPlay 안내 구독 중단을 곧바로 경로 종료로 취급하지 않고, 최신 안내의 제한된 유지와 시작 구독 재시도를 추가했습니다. 실제 종료·연결 해제는 별도로 처리합니다.
- 연결 정보 캐시와 같은 상류 도로로 돌아가는 중복 가지 처리를 보완했습니다.
- 일부 삼거리의 추가 가지를 줄이는 검사를 추가했습니다. 연결 그래프의 순환만으로 실제 교통섬이 있다고 단정하지 않습니다.

## 0.1.92

- LX2 PE 기준 엔진 외에 DN8 HEV·K5 DL3 수집본의 검증된 해시를 추가했습니다. 이후 받은 CK PE 수집본도 같은 해시로 확인됐습니다.
- 차종 이름이 아니라 정확한 엔진 프로파일로 조회합니다. 미등록 엔진의 순정 지도 조회는 계속 거부합니다.
- 파일 동일성과 정적 심볼 비교는 차량 전체 기능의 동작 보장이 아니며, 실차 조회·확대 결과와 구분합니다.

## 0.1.91

- `CompoundJunction.draw()`의 분리 클래스 로더 참조로 발생한 CarPlay `IllegalAccessError` 경로를 수정했습니다.
- 도형 해석 코드를 렌더러와 같은 로더 영역으로 분리하고, 획득 계층과는 직렬화된 기본형 데이터로 전달합니다.
- 확대 중 `LinkageError`가 발생하면 해당 확대 인스턴스를 중단하고 일반 TBT로 복귀하도록 CarPlay·AA 방어를 추가했습니다.

## 0.1.90

- 유효한 확대 카운트다운의 최소 막대를 10%로 유지하도록 수정했습니다. 순정 관찰에서 0m에도 최소 한 구간이 남는 동작을 반영했습니다.
- 무효 거리·표시 범위 밖·안내 종료의 숨김 및 순정 복원 경로는 유지했습니다.

## 0.1.89

- Korea Projection의 막대 처리와 순정 API를 비교해, 실제 순정 단속 정보 프레임에 접근 막대를 반영하는 경로를 추가했습니다.
- 정수형·객체형 API의 원본 필드를 복사하고 막대만 변경합니다. 종료·만료 때 원본을 복원하며, 순정 프레임을 받기 전에는 임의로 만들지 않습니다.
- 0.1.87의 자동 테마 재전송 실험은 제거하고 관찰만 남겼습니다.

## 0.1.88

- 짧게 연결된 여러 물리 노드를 조사해 복합 교차로와 회전교차로의 닫힌 연결을 검증하는 시험 경로를 추가했습니다.
- 노드·링크 수와 조사 시간을 제한하고, 열린 연결·복수 순환·불완전 형상·층위 충돌은 거부합니다.
- 물리 도로 그래프 진단 기록을 추가했습니다. 구현 시험 통과가 실제 교통섬 판별이나 모든 회전교차로의 실차 성공을 뜻하지 않습니다.

## 0.1.87

- 순정에서 실제 수신한 현재 부팅의 테마 값만 재사용하는 확대 막대 테마 시험을 추가했습니다. 임의 색상값은 보내지 않았습니다.
- AA 초기화 설정과 순정/폰 출력 관측을 보강했습니다.
- 테마 시험은 막대 문제의 확정 해결이 아니며, 0.1.89에서 자동 재전송을 제거했습니다.

## 0.1.86

- 전방 물리 도로별 고가·지하 속성을 추가 수집하고 새 형상 포맷에서 전달하도록 했습니다. 이전 형상 포맷도 계속 읽습니다.
- 실제 속성·안내 목표·방향이 일치할 때 고가/지하 진입 또는 옆길 확대를 선택하도록 보완했습니다.
- 당시 복합 교차로·회전교차로 전체 연결과 막대 실차 문제는 해결 완료로 분류하지 않았습니다.

## 0.1.85

- AA 회전교차로의 유효 side1 입력과 진입 이벤트, 각도 없는 경우의 대체 처리를 보완했습니다.
- 갈림길을 완만한 좌·우회전 표현으로 바꾸고, 수신한 원시 의미 정보로 방향 누락을 보완했습니다.
- 정밀 각도가 없는 회전에서 같은 쪽의 유일한 출구를 제한적으로 매칭합니다. 여러 출구가 가능하면 거부합니다.
- 명확한 고가·지하 안내에 순정 확대 선택자를 연결했습니다.
- USB 수집에서 동기 IPC 대기를 줄이고 원시 AA 입력·실제 최종 출력 인자의 제한 용량 기록을 추가했습니다.

## 0.1.84

- AA 목적지 접근 안내에서 이전 교차로명 대신 현재 도착지 주변 문구를 갱신하도록 했습니다.
- 고가도로·지하도로 표현을 시설 인식에 추가하고 부정·모호한 문구는 제외했습니다.
- 도형을 확보하지 못한 CarPlay 일반 안내에도 알려진 방향과 거리 막대를 전달합니다.
- 확대 실패 이유를 구분하고 AA 확대 저널의 USB 내보내기를 추가했습니다. 미등록 엔진 강제 로드나 네이티브 패치 소유권 우회는 하지 않았습니다.

## 0.1.83

- USB 로그 내보내기에서 무제한 과거 로그 읽기와 데몬 IPC 의존을 제거하고, 최신 자료를 먼저 제한된 크기로 기록합니다.
- 수집 단계·완료 상태·실행 버전·부팅 ID를 내보내도록 했습니다.
- API 결과 파일 쓰기 실패를 한 번 기록하고 재시도하며, 실패한 사본을 완료로 표시하지 않도록 수정했습니다.

## 0.1.82

- AA 목적지 접근을 도착 완료가 아닌 접근 아이콘112로 처리하도록 수정했습니다. 현재·다음 안내에 함께 적용했습니다.
- 목적지 안내가 시작되면 이전 회전 거리를 지우고 실제 새 거리를 기다립니다. 0m만으로 안내 완료를 만들어 내지 않습니다.

## 0.1.81

- AA 카메라 등 일시적인 방향 없는 알림이 실제 회전 안내를 즉시 대체하는 경로를 제한했습니다.
- 대기 중 알림의 거리도 함께 보류하고 실제 회전 복귀·시간 제한을 처리했습니다.
- 이는 초기 알림 경로의 수정이며, 정상 안내 복귀 뒤 늦게 들어오는 카메라 거리 문제는 1.0.1 후속 수정 대상으로 별도 확인됐습니다.

## 0.1.80

- AA 다음 안내의 메타데이터·거리·종료 표식 수신 순서에 따른 누락을 보완했습니다.
- 현재 안내의 HUD 갱신이 이미 받은 다음 안내 거리를 지우지 않도록 했습니다.
- 실제 두 번째 안내가 있는 경우에만 원시 의미 정보로 보완하고 차선 인코딩 진단을 추가했습니다.

## 0.1.79

- 확대 안내의 앱 허용 목록과 수동 앱 선택을 제거하고 CarPlay·AA가 공통 확대 설정을 사용하도록 했습니다.
- AA 회전·진출입·회전교차로 입력을 공통 렌더러에 맞게 정규화했습니다.
- 활성 안내와 최신 거리 수신이 있는 동안에만 제한된 지도 조회 요청을 갱신합니다. 없는 가지나 모르는 방향은 만들지 않습니다.

## 0.1.78

- 같은 안내의 확인된 형상을 일시적인 조회 누락·거리 흔들림 동안 제한적으로 유지하도록 했습니다. GPS·물리 대상·거리 검사는 유지합니다.
- 막대를 10% 단위로 반올림하고 휴게소 코드를 현재·다음·HUD에서183으로 통일했습니다.
- 중복·과거 시험 설정을 정리하고 회전교차로 방향 확대의 제한된 시험 경로를 추가했습니다.

## 0.1.77

- 현재 연결의 일부 목록 대신 물리 노드의 전체 인접 도로 목록을 사용하도록 보완했습니다.
- 중간 노드를 따라 전방 안내 거리와 가까운 대상을 찾는 제한된 탐색을 추가했습니다. 갈림길이 모호하거나 자료가 불완전하면 일반 TBT를 유지합니다.
- 형상에 안내 대상과 경로 거리를 묶고, 회색 도로와 파란 경로가 같은 방향 구간을 쓰도록 했습니다.
- 활성 안내 중 지도 조회 초기화와 400/1050m 시험 범위 처리를 보완했습니다.

## 0.1.76

- 지도 기반 확대 대상의 확인·유지 상태를 추가해 순간적인 후보 변화로 화면이 전환되는 현상을 줄이도록 했습니다.
- 초기 반복 확인과 근접 구간 형상 유지를 도입했습니다. 이후 0.1.77·0.1.78에서 물리 대상·유지 조건을 추가 보완했습니다.

## 0.1.75

- 활성 CarPlay NAVER/TMAP 안내가 1km 이내일 때 순정 지도 형상을 주기적으로 조회하는 확대 시험을 연결했습니다.
- 중복 JNI 조회를 막고 목표 갱신 주기를 1초로 제한했습니다. 실제 도형·거리·방향이 맞지 않으면 일반 TBT를 유지합니다.
- 요금소 접근 확대 경로를 추가했습니다. 이 단계의 주행 중 반복 조회와 화면 결과는 실차 검증 전이었습니다.

## 0.1.74

- 현재 도로와 연결 도로의 좌표 표본·길이를 읽는 형상 수집 시험을 추가했습니다.
- 작은 현재 부팅 API 결과 파일을 큰 과거 로그 수집과 별도로 먼저 내보내도록 했습니다.
- 보류한 0.1.73 성능 실험을 제외하고 API 조사 경로를 이어갔습니다.

## 0.1.73 — 적용 보류한 성능 실험

- 위치 발행을 낮은 우선순위 작업으로 옮기고, 부팅 직후 조회·큰 로그 수집을 늦추는 로컬 실험을 작성했습니다.
- API 조사를 먼저 진행하라는 요청에 따라 적용을 보류하고 시험 패키지를 0.1.72로 되돌렸습니다. 이후 버전에 이 실험 전체가 포함된 것으로 취급하지 않습니다.

## 0.1.72

- 특정 약한 정리 심볼의 조회 실패에 한해, 검증된 엔진과 실행 영역 안에서만 제한적으로 주소를 해석하는 대체 경로와 진단을 추가했습니다.
- 개발 차량의 정차 시험에서 현재 도로와 연결 목록 반환을 확인했습니다. 연결 개수만으로 교차로 형상이나 전체 ABI 안정성을 확인한 것은 아닙니다.

## 0.1.71

- 구형 Android의 라이브러리 소유 이름 차이를 처리하도록 전체 경로 또는 정확한 이름과 실행 메모리 영역을 함께 검사합니다.
- 임의의 같은 이름 라이브러리나 범위 밖 함수 주소는 계속 거부합니다.

## 0.1.70

- 구형 Android에서 지원되지 않는 `RTLD_NOLOAD` 사용을 제거하고, 이미 로드된 순정 엔진의 심볼만 조회하도록 변경했습니다.
- 함수 소속 라이브러리 검증을 유지하며 새 순정 엔진을 강제로 로드하지 않습니다.

## 0.1.69

- JNI 보조 라이브러리의 `libdl.so` 의존성을 명시해 Android 로드 시 심볼을 찾지 못하던 문제를 보완했습니다.
- 링크 전용 스텁은 배포 파일에 넣지 않고 ARM 의존성·심볼 검사를 추가했습니다.

## 0.1.68

- 루트 전용 모드 경로를 앱에서 직접 로드하던 문제를 수정했습니다.
- 초기화 단계에서 검증한 보조 라이브러리를 앱 전용 경로에 복사하고, 무결성 확인 후 그 사본을 로드합니다. 상위 디렉터리 권한은 완화하지 않습니다.

## 0.1.67

- USB가 연결된 동안 최신 로그를 주기적으로 다시 수집하고 실행 중 수집기·앱 버전과 부팅 상태를 구분하도록 했습니다.
- 순정 API 훅 설치·유효 위치 발행·조회 시작과 실패 이유의 진단을 보강했습니다.

## 0.1.66

- AA 원시 의미 패킷에서 반복되는 다음 단계와 현재 차선을 제한적으로 해석하도록 추가했습니다.
- 실제 수신한 방향·추천 상태만 반영하며, 없는 다음 거리나 포켓 차선을 만들지 않습니다.
- 유효한 원시 차선 프레임이 빈 네이티브 콜백에 지워지는 경로를 보완했습니다.

## 0.1.65

- 순정 GIS 위치 관찰과 현재 도로·즉시 연결 목록의 JNI 시험 경로를 연결했습니다.
- 기본 OFF인 정차 1회 시험, 정확한 엔진 해시 검사와 결과 유효기간 검사를 추가했습니다. 이 단계에서는 전방 교차로 확대를 출력하지 않았습니다.

## 0.1.64

- 휴게소 심볼181/182/183을 동일한 보조 인자로 비교하는 설정을 추가했습니다.
- 현재·다음 안내에 같은 선택을 적용하며, 코드 선택만으로 모든 차량의 심볼 출력이 보장되지는 않습니다.

## 0.1.63

- 확대 거리 정책을 일반400m·고속도로 진출입1050m 또는 속도별100/400/500m 중 선택하도록 추가했습니다.
- 거리 조건 외에 실제 도형·각도 수신 조건을 계속 검사합니다.

## 0.1.62

- 폰에서 받은 교차로 가지·출구 각도를 이용하는 CarPlay NAVER/TMAP 확대 시험을 추가했습니다.
- 앱 이름이 늦게 오는 경우를 위한 수동 앱 선택과 반복 각도 배열 처리를 추가했습니다.
- 속도·각도·방향이 불명확하면 일반 TBT를 유지합니다. 이 단계는 순정 지도 검색 기반 확대가 아닙니다.

## 0.1.61

- 페리 승하선 이미지를 터널로 오인했던 매핑을 제거하고 터널은 문구와 직진 안내로 되돌렸습니다.
- 당시 휴게소 출력을183 기준으로 통일하고 유효 도형을 확보하지 못한 순정 형상 재사용 시험의 기본 활성화를 해제했습니다.

## 0.1.60

- 최신 순정 형상과 폰 안내가 일치할 때 사용하는 CarPlay NAVER/TMAP 확대 시험을 추가했습니다.
- 속도별100/400/500m 조건을 적용했습니다. 후속 실차 자료에서 앱 식별과 순정 형상 확보가 충분하지 않아 이 방식만으로 확대 성공을 확인하지 못했습니다.

## 0.1.59

- 순정 교차로·확대 전환의 진단 수집 경로를 추가했습니다.
- 이 단계의 수집 기능 자체는 확대 안내를 활성화하지 않습니다. 확보한 순정 출력 인자와 화면 관찰을 후속 구현의 비교 자료로 사용했습니다.

## 0.1.58

- Keep CarPlay roundabout entry/exit text and distance live when maneuver types 6/7 omit junction metadata or exit angle. Missing angle uses non-directional type 64, with explicit roundabout text; no exit bearing is invented.
- An explicitly received roundabout exit angle can select a roundabout icon even if the junction flag is absent. Unhandled left-hand-traffic geometry remains non-directional.
- Use the current-maneuver mapping as the ahead-icon fallback, fixing slight-left/right 49/50, sharp turns and end-of-road turns disappearing from the upcoming slot.
- Route-start type 11 also retains text/distance instead of aborting output.
- 카카오내비 회전교차로 진입에서 출력이 멈추는 경로와 다음 안내의 완만한 좌·우회전 변환 누락을 보완했습니다. 각도가 없는 회전교차로는 문구·거리만 유지합니다.
- Recorded-input and startup host regressions pass. Vehicle rendering is not yet validated. Exact underpass artwork still needs received facility text or other verified facility metadata.

This release is a prerelease. The Kakao roundabout and upcoming-turn fallbacks were reproduced from captured CarPlay data, but neither has a post-0.1.58 vehicle confirmation. See [the full release notes](RELEASE-v0.1.58.md) for the CarPlay/Android Auto app matrix, known issues, installation and validation status.

## 0.1.57

- Allow received contiguous upcoming CarPlay maneuvers before the optional source name arrives; preserve active-state, list-count and received-maneuver checks.
- Use an explicitly received current-maneuver distance while waiting for the route-level countdown. Missing distance still waits instead of rendering a fabricated 0.0 km.
- Track maneuver-distance validity and reset it with maneuver state.
- CarPlay 첫 안내의 앱 이름 지연과 초기 거리 대기로 인한 표시 누락을 보완했습니다. 실제 수신 거리만 사용합니다.
- Include Kakao Navi in the received contiguous-maneuver selection: recorded input at 08:32:07 includes upcoming index 1 and its 1600 m distance.
- Seventeen startup regression assertions pass, including the recorded first-route snapshot and Naver's 65 m initial maneuver. Vehicle validation remains pending; absent AA ahead/lane data remains unresolved.

## 0.1.56

- Replay complete Android Auto current-turn and trip data received before active status when guidance becomes active.
- Clear cached route state after navigation ends; distance-only callbacks cannot resurrect the previous turn.
- Add host tests for first-activation ordering, repeated status, and restarting without stale route data.
- AA 시작 전 수신한 안내의 표시 누락을 보완했습니다. 실차 검증은 남아 있습니다.
- Missing AA ahead maneuvers/recommended lanes remain unresolved: latest callbacks contain no ahead events and empty lane data. This release does not fabricate either feature.
- CarPlay first-route source-name gating and initial-distance availability remain separate open issues.

## 0.1.55

- Merge general and highlighted CarPlay lane directions instead of assuming highlighted directions are already in the general list.
- Fix recorded TMAP straight/left and straight/right lanes losing the straight arrow and becoming entirely gray.
- TMAP의 일반 방향과 강조 방향을 합쳐 직좌·직우 복합 차선과 방향별 강조를 보존합니다.
- Pocket markers still require fresh, compatible factory lane hints. No such factory frames were observed during the latest TMAP drive; this release does not fabricate pocket markers.
- Recorded-input host regressions pass; vehicle display validation remains pending.

## 0.1.54

- Fix service-area selection being applied only to the main TBT call. Current, HUD and ahead mappings now use the same selected code.
- Default service-area code is183.
- 본문183·안내 목록181로 불일치하던 회귀 오류를 수정했습니다. 실차 재검증이 필요합니다.

## 0.1.53

- Default CarPlay service-area output is now 182 with baseline auxiliary arguments.
- Added a 183 fallback in the new service_area_display setting. Existing diagnostic selections do not override this new setting.
- 휴게소는 우선182로 시험하고, 미출력 또는 졸음쉼터 그림이면183으로 전환할 수 있습니다.

## 0.1.52

- Added controlled CarPlay service-area argument profiles for codes181/182/183.
- Default test181_aux uses sideStreet65535,direction255,bar15,error-2. These are experimental hypotheses, not a recovered native Java call.
- Capture all nine native TBT API arguments and Binder-proxy transport arguments, with facility text for correlation.
- Added profile range/fallback checks. Vehicle rendering and the auxiliary-argument hypothesis remain unverified.
- 휴게소 추가 인자 비교와 순정 API/Binder 인자9개 진단을 추가했습니다. 실제 그림 표시 여부는 미검증입니다.

## 0.1.51

- Recover CarPlay TMAP lane geometry when only highlighted directions are supplied.
- Restore Android Auto road text from the intact UTF-8 NavigationState packet.
- Remove the failed service-area type 13 trial that opened the junction display.
- Recognize TG suffixes and use domestic TG type 184. Add dump-derived tunnel type 176.
- Extend named overpass/underpass side-road handling to received maneuver types 49/50.
- All changes require vehicle verification. Standalone service-area artwork, Android Auto missing next turns/lanes, and pocket-lane availability remain unresolved.
- TMAP 화살표 누락과 Android Auto 끝글자 손상을 보완하고, 휴게소 유형 13 시험을 제거했습니다. TG·터널·옆길 처리를 추가했습니다. 전체 요구사항 해결은 미완료입니다.

## 0.1.50

- Added a CarPlay service-area cluster type 13 trial, enabled by default and switchable in settings.
- Based on the native SetCaseServiceArea legacy path. The physical cluster symbol is unverified. Native progress and all legacy parameter semantics are not reconstructed.
- HUD and next-turn symbols keep their existing modern encoding.
- CarPlay 휴게소에 기존 방식 유형 13을 시험하는 설정을 추가했습니다. 끄면 유형 181로 복귀합니다. 실차 표시와 기존 방식의 전체 인자 재현은 미확인입니다.

## 0.1.49

- Android Auto ends with cluster type 8, matching the observed LX2 PE native termination sequence.
- Added display protection leases to Android Auto and periodic remaining-distance refresh while the lease integration is active.
- CarPlay TMAP can use the received adjacent maneuver as the next-turn candidate, following its observed contiguous snapshots.
- Preserve supplied lane geometry when all received lane directions are unavailable. Recommendations continue to use received status.
- Added raw Android Auto callback text, lane payloads and native navigation dumps to mod diagnostics.
- Android Auto 종료 로딩, 남은 거리 덮어쓰기, TMAP 다음 안내 및 차선 숨김 처리를 보완했습니다.
- Korean text rendering, service-area rendering, Android Auto TMAP loading and app-specific missing fields remain under diagnosis. Vehicle verification is pending.

## 0.1.48

- Added Android Auto navigation callbacks and the HMG-API AaTurns conversion implementation.
- Added ENUM cluster capability selection, two verified native receiver recipes, and route/connection lifecycle hooks.
- Added an Android Auto TBT toggle, enabled by default.
- Android Auto 길안내 수신·계기판 변환 및 연결 수명주기 처리를 추가했습니다.
- Android Auto는 시험 구현이며 실차 동작 확인이 필요합니다. 휴게소 표시 및 앱별 미해결 증상은 남아 있습니다.
- Third-party attribution is recorded in NOTICE.md.

## 0.1.47

- Wait for a received current-turn distance instead of sending an uninitialized zero. Use the received numeric meter value when the formatted distance is unavailable.
- Refresh the next-maneuver bound when the route count arrives separately from the maneuver list.
- Handle straight maneuver type 3 at a roundabout when a valid exit angle is received.
- Preserve route, trip, source-change, and native-fix records in exported history.
- Initial fixes for reported symptoms. Remaining-distance loss, intermittent pocket markers, service-area rendering, and TMAP/Kakao guidance still require diagnosis. Android Auto TBT remains unimplemented.
- 초기 거리 0 표시와 늦게 도착하는 다음 안내 개수 처리를 보완하고, 출구 각도가 있는 회전교차로 type 3 처리를 추가했습니다.
- 전체 증상 해결 및 실차 검증은 미완료입니다. Android Auto TBT는 미구현입니다.

## 0.1.46

- Removed model, region, and firmware restrictions from both the HMG-API update and NEMESIS installer.
- Restricted the native lane fixes to the verified iAP2 library build. CarPlay guidance continues through the existing HMG-API hooks.
- HMG-API 업데이트와 NEMESIS 최초 설치의 차종·국가·펌웨어 제한을 해제했습니다.
- 검증된 순정 iAP2 라이브러리에서만 네이티브 차선 보정을 적용합니다. CarPlay 길안내는 기존 HMG-API 훅을 사용합니다.

## English overview · 0.1.45

Phone Projection TBT is the new project/mod name. CarPlay uses a shared wired/wireless receiver; Android Auto TBT is planned and is not implemented. Existing ID and signer remain compatible. New log name: `phone-projection-tbt.log`.

Since public 0.1.41: corrected composite lane shapes and per-direction colors, added guarded factory pocket markers, updated highway facility mappings, improved USB diagnostics, and introduced an unverified alternative service-area icon. Overpass/underpass and unprotected-turn attributes are not guessed when absent. Latest rendering changes remain under vehicle verification. See the bilingual [0.1.45 release notes](RELEASE-v0.1.45.md).

## 0.1.45 · 2026-11-09 · Phone Projection TBT

- 프로젝트·모드 표시 이름과 패키지 파일명을 Phone Projection TBT로 변경했습니다.
- 유선·무선 CarPlay를 포함하는 설명으로 정리하고 Android Auto TBT는 지원 예정으로 명시했습니다.
- 진입 클래스와 Java 네임스페이스를 projection / projection.carplay로 정리했습니다.
- 새 USB 로그 이름은 `phone-projection-tbt.log`입니다.
- 기존 설치의 모드 ID, 서명자와 설정 키는 유지합니다.
- 0.1.44의 차선·시설 표시 동작을 유지합니다.

## 0.1.42–0.1.44 · 로컬 시험 빌드

- 순정 리소스와 주행 로그를 기준으로 차선별 색상 및 복합 화살표를 수정했습니다.
- 좌회전·유턴 차선은 같은 주행에서 확보한 순정 조합을 적용했습니다.
- 최신 순정 데이터의 차선 수·방향이 일치할 때 포켓 차선 표시를 보완합니다.
- 휴게소 진출 도식 대신 대체 순정 코드 181을 시험 적용합니다. 단독 심볼인지는 실차 미확인입니다.
- 구분 정보가 없는 고가·지하차도와 비보호 좌회전은 임의로 추정하지 않습니다.
- USB 로그 수집에서 차량에 없는 tail 명령 의존성을 제거했습니다.

## 0.1.41 · 2026-11-08 · 시험판

- 차선의 각 화살표에 2비트 표시 상태를 배정합니다. 회색은 경로에 맞지 않는 방향, 흰색은 주행 가능한 방향, 파란색은 추천 방향입니다. 복합 차선에서 일부 화살표가 사라지거나 잘못 강조되던 처리를 수정했습니다.
- 차선 안내가 계속 유효한 동안 표시와 점유 시간을 갱신해, 최초 표시 후 12초가 지나 사라지던 문제를 수정했습니다.
- 계기판에서 그림이 비어 있던 좌측 고속도로 진입 코드는 표시 가능한 좌측 분기 화살표로 대체합니다.
- 고가차도 진입 및 우측 옆길 문구에 대한 시험 매핑을 포함합니다. 고가차도·옆길 전용 그림은 실차 확인이 필요합니다.
- 순정 내비의 계기판 전송 데이터 및 내부 전달 함수 5종을 관찰합니다. 회전하는 진단 로그를 보관하고 USB 연결 시 기존 TBT 로그와 함께 `stock-guidance.log`로 내보냅니다.

실제 수신 로그의 차선 상태·복합 방향 재현 검사, 컴파일, USB 로그 저장 검사와 서명 검증을 통과했습니다. 0.1.41의 실제 계기판 색상과 표시 지속 여부는 재확인 중입니다.

휴게소 전용 심볼 표시는 미해결입니다. 추가한 내부 전달 함수에서 휴게소 심볼 식별값을 얻을 수 있는지도 확인 중입니다.
