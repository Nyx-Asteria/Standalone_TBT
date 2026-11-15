
# HMG Phone Projection TBT

[한국어](README.md) · [English](README.en.md) · [Documentation (Wiki)](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki)

**Apple CarPlay 및 Android Auto의 내비게이션 길안내 정보를 현대·기아·제네시스 차량의 계기판과 HUD로 전달하는 HMG-API 커뮤니티 모드입니다.**

Phone Projection에서 제공하는 Turn-by-Turn(TBT) 안내 정보를 수신하고, 차량의 `NavigationCluster` 인터페이스를 통해 계기판 및 HUD에 전달합니다.

CarPlay와 Android Auto의 수신 경로를 각각 구현했으며, 계기판 출력 계층을 공유합니다.

> [!IMPORTANT]
> **이 모드는 무선 CarPlay 또는 무선 Android Auto를 활성화하지 않습니다.**
>
> 유선 또는 무선 Phone Projection 연결이 이미 정상적으로 작동하고 있어야 합니다.
>
> 이 프로젝트는 비공식 커뮤니티 모드이며, 모든 차량과 펌웨어에서의 작동을 보장하지 않습니다.

**Latest Release:** [v0.1.58 (Prerelease)](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/releases/tag/v0.1.58)

---

## 주요 기능

| 기능 | 설명 |
| --- | --- |
| 현재 안내 | 현재 진행 중인 회전 및 경로 안내 |
| 다음 안내 | 다음 안내 지점의 방향과 거리 |
| 안내 지점 거리 | 현재 안내 지점까지의 남은 거리 |
| 목적지 잔여 거리 | 최종 목적지까지의 남은 거리 |
| 차선 안내 | 차선별 진행 방향 및 추천 상태 |
| 특수 안내 | 일부 회전교차로 및 도로 시설 안내 |
| 계기판 출력 | 계기판 내비게이션 영역에 TBT 표시 |
| HUD 출력 | 지원되는 차량의 HUD에 안내 표시 |

실제 표시 가능한 정보는 내비게이션 앱이 제공하는 데이터, 차량의 HMG-API 맵 및 계기판 펌웨어에 따라 달라질 수 있습니다.

### 차선 표시

| 색상 | 의미 |
| --- | --- |
| 회색 | 경로에서 벗어나는 방향 |
| 흰색 | 경로상 주행 가능한 방향 |
| 파란색 | 내비게이션 앱에서 추천하는 방향 |

복합 차선의 각 화살표 방향별 상태를 처리하며, 일부 포켓 차선은 호환되는 최신 순정 차선 데이터를 이용합니다.

앱에서 제공하지 않는 추천 차선이나 시설 정보는 임의로 생성하지 않습니다.

[지원 기능 자세히 보기](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Features)

---

## 앱별 지원 현황

아래 표는 현재까지의 구현 및 실차 검증 현황입니다.

**기본 TBT의 정상 동작과 모든 세부 기능의 정상 동작은 구분합니다.**

| 연결 방식 | 내비게이션 앱 | 기본 TBT | 주요 상태 |
| --- | --- | --- | --- |
| CarPlay | NAVER 지도 | ✅ 정상 | 현재·다음 안내 및 차선 데이터 처리 |
| CarPlay | TMAP | ✅ 정상 | 일부 차선·특수 심볼 개선 필요 |
| CarPlay | 카카오내비 | 🔧 구현 | v0.1.58 수정 사항 실차 재검증 필요 |
| Android Auto | NAVER 지도 | ✅ 정상 | 잔여 거리·한글 안내·안내 종료 정상 |
| Android Auto | TMAP | ✅ 정상 | 잔여 거리·한글 안내·안내 종료 정상 |
| Android Auto | 카카오내비 | ⬜ 미검증 | 공통 수신 경로만 구현 |

### Apple CarPlay

NAVER 지도와 TMAP의 기본 TBT 동작이 실차에서 확인되었습니다.

카카오내비도 현재·다음 안내 수신 및 변환 기능이 구현되어 있으며, v0.1.58에서 회전교차로 및 일부 다음 안내의 처리 방식을 개선했습니다.

다만 카카오내비의 최신 수정 사항은 실차 재검증이 필요합니다.

### Android Auto

**NAVER 지도와 TMAP 양쪽에서 다음 기능의 정상 동작이 확인되었습니다.**

- 기본 TBT 안내
- 목적지 잔여 거리 표시
- 한글 안내 문구 표시
- 경로 안내 종료 및 계기판 상태 정리

다음 안내 및 추천 차선 등의 세부 기능은 추가 검증이 필요합니다.

Android Auto 카카오내비는 아직 앱별 실차 검증 결과가 없습니다.

> [!NOTE]
> 일부 기능의 과거 오류 기록은 최신 실차 시험 결과와 다를 수 있습니다. 위 표는 이후 확인된 동작 결과를 반영하며, 특정 차량에서의 검증이 모든 차종의 호환성을 보장하지는 않습니다.

[앱 및 기능별 호환성 자세히 보기](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Compatibility)

---

## 차량 호환성

현재 다음 차량에서 동작이 확인되었습니다. 표에 없는 앱·연결 방식이나 세부 기능은 별도로 검증되지 않았을 수 있습니다.

| 차량 | AVN | 소프트웨어 | 검증 구분 |
| --- | --- | --- | --- |
| Hyundai Palisade LX2 PE (2023) | Gen5W | `LX2PE.KOR.0000.V039.001.260902` | 개발 차량 |
| Hyundai Palisade (2022) | Gen5W (세부 사양 미확인) | 미확인 | 사용자 제보 |
| Kia Stinger Meister 3.3 (2023) | Gen5W S5W_M | `CK_PE_22.KOR.S5W_M.V039.001.260902` | 사용자 제보 |
| Kia K5 DL3 1.6T (2022) | Gen5W | `DL322.KOR.0000.V039.001.260902` | 사용자 제보 |
| Hyundai Sonata DN8 HEV (2022) | Gen5W | `DN822HEV.KOR.0000.V039.001.260902` | 사용자 제보 |

### Hyundai Palisade LX2 PE (2023)

개발 및 실차 검증 기준 차량입니다.

CarPlay NAVER 지도·TMAP 및 Android Auto NAVER 지도·TMAP의 주요 TBT 기능이 시험되었습니다.

### Hyundai Palisade (2022)

**유선 CarPlay + TMAP**의 기본 TBT 정상 동작이 제보되었습니다.

계기판·HUD별 세부 기능과 AVN 전체 소프트웨어 버전은 확인되지 않았습니다.

### Kia Stinger Meister 3.3 (2023)

외부 사용자 제보를 통해 다음 조합의 정상 동작이 확인되었습니다.

- 유선 CarPlay + NAVER 지도: 계기판·HUD 정상
- 무선 CarPlay + NAVER 지도: 계기판·HUD 정상
- Phone Projection TBT v0.1.46 및 v0.1.58 동작 사례

스팅어의 TMAP, 카카오내비 및 Android Auto는 아직 시험되지 않았습니다.

[Stinger Meister 호환성 제보 — Issue #2](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/issues/2)

### Kia K5 DL3 1.6T (2022)

**Android Auto + NAVER 지도**의 기본 TBT 동작이 확인되었습니다(사용 버전: v0.1.58).

다만 해당 버전에서 과속카메라 접근 시 기존 안내가 일시적으로 직진 표시로 변경되는 현상이 제보되었습니다. 다른 세부 기능까지 모두 검증됐다는 의미는 아닙니다.

### Hyundai Sonata DN8 HEV (2022)

**Android Auto + NAVER 지도**의 기본 TBT 동작이 확인되었습니다(사용 버전: v0.1.58).

다만 해당 버전에서 실제 목적지까지 약 1.3km가 남았는데 계기판·HUD에 도착으로 표시되는 현상이 제보되었습니다. 다른 세부 기능까지 모두 검증됐다는 의미는 아닙니다.

> 설치 패키지는 차종·국가·펌웨어를 제한하지 않지만, 이는 모든 차량에 대한 호환성 보증이 아닙니다. 네이티브 수정은 확인된 라이브러리 빌드에서만 적용됩니다.

[차량별 상세 호환성 보기](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Compatibility)

---

## 설치 및 업데이트

### 요구 사항

- [HMG-API](https://github.com/VysseCodes/HMG-API) 1.0.2 이상
- 차량에 적합한 HMG-API 맵
- `Hooks on (map checked)` 상태
- 정상 작동하는 CarPlay 또는 Android Auto 연결

차량에 대한 HMG-API 맵이 없다면 `Map this car from its files` 기능으로 직접 생성할 수 있습니다.

### 최초 설치

1. HMG-API를 설치하고 차량 맵을 확인합니다.
2. `Hooks on (map checked)` 상태를 확인합니다.
3. NEMESIS에서 다음 최초 설치 패키지를 설치합니다.
4. AVN을 재시작하고 TBT 기능을 확인합니다.

**설치 파일**

`phone-projection-tbt-0.1.58-nemesis-package.zip`

### 기존 버전 업데이트

다음 업데이트 ZIP을 압축 해제하지 않고 USB의 `HMGAPI/updates/` 경로에 복사합니다.

`phone-projection-tbt-0.1.58.zip`

이전 Phone Projection TBT 업데이트 ZIP이 있다면 제거합니다.

정차 상태에서 HMG-API의 다음 기능을 실행합니다.

1. `Check USB updates`
2. `Apply changes`
3. `Restart Unit`

### 배포 파일

| 파일 | 용도 |
| --- | --- |
| `phone-projection-tbt-0.1.58-nemesis-package.zip` | 최초 설치 |
| `phone-projection-tbt-0.1.58.zip` | 기존 설치 업데이트 |
| `phone-projection-tbt-0.1.58-source.zip` | 소스 코드 및 개발 자료 |

[설치 및 업데이트 가이드](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Installation)

---

## Phone Projection 모드와 함께 사용

별도의 Phone Projection 모드와 함께 사용하는 경우 중복 TBT 출력을 방지해야 합니다.

다음 기능을 비활성화하는 것을 권장합니다.

`Turn-by-turn on the cluster and HUD`

Phone Projection TBT에서 계기판 및 HUD 안내를 담당하도록 설정합니다.

### 알려진 네이티브 패치 충돌

Korea Projection과 Phone Projection TBT 사이에서 Android Auto 관련 네이티브 패치 충돌이 보고되었습니다.

대상 라이브러리:

`libautoreceiver_infobank.so`

보고된 오류:

```text
aa-tbt-48ee793a: conflict with wired-carplay-tbt-lx2pe's aa-tbt-48ee793a
```

Korea Projection의 TBT 기능을 비활성화하는 것은 중복 출력을 방지하기 위한 설정이며, 네이티브 패치 충돌 자체를 해결하지는 않습니다.

[알려진 문제 및 제한 사항](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Known-Issues)

---

## 알려진 제한 사항

현재 다음 기능은 개선 또는 추가 검증이 필요합니다.

- **휴게소 단독 심볼:** 미해결. 코드 `183`은 진출로 도식으로 표시되며, `181`·`182`의 단독 심볼 출력은 확인되지 않았습니다.
- **일부 특수 시설 심볼:** 요금소, 터널, 고가도로 및 지하차도 안내의 일부 표현이 제한됩니다.
- **차선 안내:** 추천·주행 가능 방향의 일부 표시가 앱 데이터에 따라 제한될 수 있습니다.
- **포켓 차선:** 최신 순정 차선 데이터와 차선 구성의 일치가 필요합니다.
- **Android Auto 다음 안내:** 수신 경로는 구현되어 있으나 앱별 추가 검증이 필요합니다.
- **Android Auto 추천 차선:** 실제 추천 정보 및 계기판 출력의 추가 검증이 필요합니다.
- **CarPlay 카카오내비:** v0.1.58의 일부 최신 수정 사항은 실차 확인이 필요합니다.

자세한 내용은 [Known Issues](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Known-Issues)를 참고하세요.

---

## Documentation

설치 방법, 차량 호환성, 기능 설명 및 문제 해결은 GitHub Wiki에서 확인할 수 있습니다.

| 문서 | 내용 |
| --- | --- |
| [Home](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki) | 프로젝트 소개 및 빠른 시작 |
| [Installation](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Installation) | 최초 설치 및 업데이트 |
| [Compatibility](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Compatibility) | 차량·AVN·앱 호환성 |
| [Features](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Features) | 기능 및 표시 방식 |
| [Known Issues](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Known-Issues) | 알려진 문제와 제한 사항 |
| [Troubleshooting](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Troubleshooting) | 오류 해결 |
| [Logs and Issue Reporting](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Logs-and-Issue-Reporting) | 로그 수집 및 오류 제보 |
| [Development](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Development) | 개발 구조 및 빌드 |

---

## 문제 신고 및 호환성 제보

새로운 차종에서 정상 작동했거나 문제가 발생했다면 GitHub Issue를 작성해 주세요.

[Create a New Issue](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/issues/new)

권장 제목:

`[국가 / 차종 코드 / AVN 펌웨어] 증상`

가능하면 차량 정보, AVN 전체 버전, HMG-API 및 TBT 버전, 내비게이션 앱, 연결 방식, 재현 방법과 발생 시각을 포함해 주세요.

**개인정보 주의:** TBT 로그에는 도로명, 목적지, 주소, 위치 좌표 및 이동 경로가 포함될 수 있습니다. 공개 전에 개인의 위치를 식별할 수 있는 정보를 반드시 마스킹해 주세요.

[로그 수집 및 제보 방법](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Logs-and-Issue-Reporting)

---

## Development

이 프로젝트는 HMG-API의 훅 및 계기판 인터페이스를 사용합니다.

공개 v0.1.58 릴리스에는 소스 코드와 호스트 회귀 검사 자료가 포함되어 있습니다.

**개발 시 주의:** 현재 `main` 브랜치의 `mod.json`은 0.1.46을 기록하고 있으므로, 최신 v0.1.58 구현을 확인하려면 해당 릴리스의 소스 아카이브를 기준으로 확인해 주세요.

- [Build Instructions](BUILD.md)
- [Changelog](CHANGELOG.md)
- [Release v0.1.58](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/releases/tag/v0.1.58)
- [Development Wiki](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Development)

**License:** [GNU AGPL-3.0](LICENSE)

---

*HMG Phone Projection TBT is an unofficial community project and is not affiliated with or endorsed by Hyundai Motor Company, Kia, or Genesis.*
