package com.example.chalkadoc.partnership;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.example.chalkadoc.R;
import com.example.chalkadoc.home.HospitalInfoActivity;
import com.example.chalkadoc.listview.EyesData;
import com.example.chalkadoc.listview.HospitalAdapter;
import com.example.chalkadoc.listview.JHospitalInfoActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.lang.reflect.Type;

public class PartnershipActivity extends AppCompatActivity implements OnMapReadyCallback {

    // yoon
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback locationCallback;
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private boolean firstLocationUpdate = true;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TextView hospitalName, hospitalAddress, hospitalPhone, hospitalHours, hospitalCategory;
    private Map<Marker, EyesData> markerEyesMap = new HashMap<>();
    private List<EyesData> hospitalList = new ArrayList<>();
    private HospitalAdapter listAdapter;
    private Location currentLocation; // 현재 위치를 저장할 변수
    private String currentCategory = ""; // 현재 선택된 카테고리 저장
    private int startValue = 0;     // 시작 시, 마커 띄울 때 씀.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partnership);

        ImageView ivOption = findViewById(R.id.iv_option);
        Button zoomInButton = findViewById(R.id.zoom_in_button);
        Button zoomOutButton = findViewById(R.id.zoom_out_button);
        Button btnDistance = findViewById(R.id.btn_distance);
        Button btnDistance2 = findViewById(R.id.btn_distance2);
        View bottomSheet = findViewById(R.id.bottom_sheet);

        startValue = getIntent().getIntExtra("startValue", 0);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight(300);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setPeekHeight(300);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // 슬라이드될 때 처리할 내용
            }
        });


        // 지도 줌인
        zoomInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.zoomIn());
                }
            }
        });

        // 지도 줌아웃
        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.zoomOut());
                }
            }
        });

        // 내 주변 버튼 클릭
        btnDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBottomSheet();
                switch (currentCategory) {
                    case "eye_clinic":
                        loadNearbyMarkersAndHospitals(R.raw.eyes, BitmapDescriptorFactory.HUE_BLUE);
                        break;
                    case "dermatology":
                        loadNearbyMarkersAndHospitals(R.raw.skin, BitmapDescriptorFactory.HUE_ROSE);
                        break;
                    case "dentist":
                        loadNearbyMarkersAndHospitals(R.raw.teeth, BitmapDescriptorFactory.HUE_ORANGE);
                        break;
                    default:
                        Toast.makeText(PartnershipActivity.this, "Please select a category first", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        // 오른쪽 상단 옵션 버튼 클릭 -> 안과, 피부과, 치과 나옴
        ivOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(PartnershipActivity.this, v); // v는 클릭된 뷰를 의미
                // 메뉴 인플레이터를 사용하여 팝업 메뉴에 메뉴 리소스를 추가
                popupMenu.getMenuInflater().inflate(R.menu.option_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (getSelectedMenu(item)) {
                            case 1:
                                // 안과
                                currentCategory = "eye_clinic";
                                toggleBottomSheet();
                                loadMarkersAndHospitals(R.raw.eyes, BitmapDescriptorFactory.HUE_BLUE);
                                break;
                            case 2:
                                // 피부과
                                currentCategory = "dermatology";
                                toggleBottomSheet();
                                loadMarkersAndHospitals(R.raw.skin, BitmapDescriptorFactory.HUE_ROSE);
                                break;
                            case 3:
                                // 치과
                                currentCategory = "dentist";
                                toggleBottomSheet();
                                loadMarkersAndHospitals(R.raw.teeth, BitmapDescriptorFactory.HUE_ORANGE);
                                break;
                            default:
                                Toast.makeText(getApplication(), "선택하지 않음", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });

        // 거리순 버튼 클릭
        btnDistance2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBottomSheet();
                switch (currentCategory) {
                    case "eye_clinic":
                        loadClosestMarkersAndHospitals(R.raw.eyes, BitmapDescriptorFactory.HUE_BLUE);
                        break;
                    case "dermatology":
                        loadClosestMarkersAndHospitals(R.raw.skin, BitmapDescriptorFactory.HUE_ROSE);
                        break;
                    case "dentist":
                        loadClosestMarkersAndHospitals(R.raw.teeth, BitmapDescriptorFactory.HUE_ORANGE);
                        break;
                    default:
                        Toast.makeText(PartnershipActivity.this, "Please select a category first", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocationOnMap(location);
                }
            }
        };

        setupDragHandle();
        setupListView();

        // 제휴병원 데이터를 로드합니다.
        loadMarkersAndHospitals(R.raw.jhospitals, BitmapDescriptorFactory.HUE_RED);

    }

    // 옵션에서 버튼 눌렀을 때 사용하는 메소드. 아이디 값에 따른 정수를 리턴함.
    private int getSelectedMenu(@NonNull MenuItem item) {
        int result = 0;

        if (item.getItemId() == R.id.btn_eye_clinic)
            result = 1;
        if (item.getItemId() == R.id.btn_dermatology)
            result = 2;
        if (item.getItemId() == R.id.btn_dentist)
            result = 3;

        return result;
    }

    private void loadClosestMarkersAndHospitals(int jsonResourceId, float color) {
        try {
            InputStream inputStream = getResources().openRawResource(jsonResourceId);
            String json = new java.util.Scanner(inputStream).useDelimiter("\\A").next();

            Gson gson = new Gson();
            Type listType = new TypeToken<List<EyesData>>() {}.getType();
            List<EyesData> clinics = gson.fromJson(json, listType);

            // 제휴 병원 데이터 추가 로드
            List<EyesData> jhospitals = loadJhospitals();
            clinics.addAll(jhospitals);

            if (currentLocation != null) {
                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                hospitalList.clear();
                mMap.clear(); // 기존 마커 제거
                markerEyesMap.clear();

                List<EyesData> nearbyClinics = new ArrayList<>();
                for (EyesData clinic : clinics) {
                    LatLng clinicLatLng = new LatLng(clinic.get위도(), clinic.get경도());
                    float distance = calculateDistance(currentLatLng, clinicLatLng);
                    if (distance <= 2000) { // 2km 이내의 병원
                        clinic.setDistance(distance);
                        nearbyClinics.add(clinic);
                    }
                }

                // 거리순으로 병원 목록 정렬
                Collections.sort(nearbyClinics, new Comparator<EyesData>() {
                    @Override
                    public int compare(EyesData o1, EyesData o2) {
                        return Float.compare(o1.getDistance(), o2.getDistance()); // 오름차순 정렬
                    }
                });

                // 정렬된 병원 목록으로 마커 추가
                for (EyesData clinic : nearbyClinics) {
                    LatLng clinicLatLng = new LatLng(clinic.get위도(), clinic.get경도());
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(clinicLatLng)
                            .title(clinic.get이름())
                            .snippet(clinic.get도로명주소())
                            .icon(BitmapDescriptorFactory.defaultMarker(color)));
                    markerEyesMap.put(marker, clinic); // Marker와 병원 정보를 매핑
                    Log.d("PartnershipActivity", "Marker added: " + clinic.get이름() + " at " + clinicLatLng.toString());
                }

                listAdapter.updateHospitals(nearbyClinics);
                listAdapter.setDistanceMode(true); // distanceMode 활성화

                // 병원 리스트 로깅
                for (EyesData clinic : nearbyClinics) {
                    Log.d("PartnershipActivity", "Hospital: " + clinic.get이름() + ", Distance: " + clinic.getDistance());
                }
            }
        } catch (Exception e) {
            Log.e("PartnershipActivity", "JSON 파일 읽기 오류", e);
        }
    }


    private void setupDragHandle() {
        View dragHandle = findViewById(R.id.drag_handle);
        dragHandle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
    }

    private void toggleBottomSheet() {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setPeekHeight(200);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private void setupListView() {
        ListView listView = findViewById(R.id.listView_list);
        listAdapter = new HospitalAdapter(this, new ArrayList<>());  // HospitalAdapter 사용
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EyesData selectedHospital = (EyesData) listAdapter.getItem(position);
                Intent intent;
                if (selectedHospital.isPartnered()) {
                    intent = new Intent(PartnershipActivity.this, JHospitalInfoActivity.class);  // 제휴 병원 정보 액티비티
                } else {
                    intent = new Intent(PartnershipActivity.this, HospitalInfoActivity.class);  // 일반 병원 정보 액티비티
                }
                intent.putExtra("hospital_name", selectedHospital.get이름());
                intent.putExtra("hospital_address", selectedHospital.get주소());  // 여기서 '도로명주소' 대신 '주소' 사용
                intent.putExtra("hospital_phone", selectedHospital.get일반전화());
                intent.putExtra("hospital_hours", selectedHospital.get영업시간());  // 추가된 부분
                intent.putExtra("hospital_category", selectedHospital.get카테고리());
                intent.putExtra("hospital_image_url", selectedHospital.get썸네일이미지URL());
                intent.putExtra("partnership_info", selectedHospital.get제휴정보());  // 제휴 정보 추가
                intent.putExtra("partnership_apply", selectedHospital.get제휴_신청_방법());  // 제휴 신청 방법 추가
                startActivity(intent);
            }
        });
    }

    private float calculateDistance(LatLng start, LatLng end) {
        float[] results = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
        return results[0];
    }

    private void loadNearbyMarkersAndHospitals(int jsonResourceId, float color) {
        try {
            InputStream inputStream = getResources().openRawResource(jsonResourceId);
            String json = new java.util.Scanner(inputStream).useDelimiter("\\A").next();

            Gson gson = new Gson();
            Type listType = new TypeToken<List<EyesData>>() {}.getType();
            List<EyesData> clinics = gson.fromJson(json, listType);

            // 제휴 병원 데이터 추가 로드
            List<EyesData> jhospitals = loadJhospitals();
            clinics.addAll(jhospitals);

            if (currentLocation != null) {
                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                hospitalList.clear();
                mMap.clear(); // 기존 마커 제거
                markerEyesMap.clear();

                for (EyesData clinic : clinics) {
                    LatLng clinicLatLng = new LatLng(clinic.get위도(), clinic.get경도());
                    float distance = calculateDistance(currentLatLng, clinicLatLng);
                    if (distance <= 2000) { // 2km 이내의 병원
                        hospitalList.add(clinic);
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(clinicLatLng)
                                .title(clinic.get이름())
                                .snippet(clinic.get도로명주소())
                                .icon(BitmapDescriptorFactory.defaultMarker(color)));
                        markerEyesMap.put(marker, clinic); // Marker와 병원 정보를 매핑
                        Log.d("PartnershipActivity", "Marker added: " + clinic.get이름() + " at " + clinicLatLng.toString());
                    }
                }

                Collections.sort(hospitalList, new Comparator<EyesData>() {
                    @Override
                    public int compare(EyesData o1, EyesData o2) {
                        int reviewCount1 = Integer.parseInt(o1.get방문자_리뷰수() != null ? o1.get방문자_리뷰수() : "0");
                        int reviewCount2 = Integer.parseInt(o2.get방문자_리뷰수() != null ? o2.get방문자_리뷰수() : "0");
                        return reviewCount2 - reviewCount1; // 내림차순 정렬
                    }
                });

                // 정렬된 병원 목록으로 마커 추가
                for (EyesData clinic : hospitalList) {
                    LatLng clinicLatLng = new LatLng(clinic.get위도(), clinic.get경도());
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(clinicLatLng)
                            .title(clinic.get이름())
                            .snippet(clinic.get도로명주소())
                            .icon(BitmapDescriptorFactory.defaultMarker(color)));
                    markerEyesMap.put(marker, clinic); // Marker와 병원 정보를 매핑
                    Log.d("PartnershipActivity", "Marker added: " + clinic.get이름() + " at " + clinicLatLng.toString());
                }

                listAdapter.updateHospitals(hospitalList);

                // 병원 리스트 로깅
                for (EyesData clinic : hospitalList) {
                    Log.d("PartnershipActivity", "Hospital: " + clinic.get이름() + ", Visitor Reviews: " + clinic.get방문자_리뷰수());
                }
            }
        } catch (Exception e) {
            Log.e("PartnershipActivity", "Error reading JSON file", e);
        }
    }

    // 기존의 loadMarkersAndHospitals 메서드 변경
    private void loadMarkersAndHospitals(int jsonResourceId, float color) {
        try {
            InputStream inputStream = getResources().openRawResource(jsonResourceId);
            String json = new java.util.Scanner(inputStream).useDelimiter("\\A").next();

            Gson gson = new Gson();
            Type listType = new TypeToken<List<EyesData>>() {}.getType();
            List<EyesData> clinics = gson.fromJson(json, listType);

            // 제휴 병원 데이터 추가 로드
            List<EyesData> jhospitals = loadJhospitals();
            clinics.addAll(jhospitals);

            mMap.clear(); // 기존 마커 제거
            markerEyesMap.clear();

            // 제휴 병원 리스트
            List<EyesData> partneredClinics = new ArrayList<>();
            // 일반 병원 리스트
            List<EyesData> generalClinics = new ArrayList<>();

            for (EyesData clinic : clinics) {
                if (clinic.isPartnered()) {
                    partneredClinics.add(clinic);
                } else {
                    generalClinics.add(clinic);
                }
            }

            // 리뷰 수에 따라 일반 병원 정렬
            Collections.sort(generalClinics, new Comparator<EyesData>() {
                @Override
                public int compare(EyesData o1, EyesData o2) {
                    int reviewCount1 = Integer.parseInt(o1.get방문자_리뷰수() != null ? o1.get방문자_리뷰수() : "0");
                    int reviewCount2 = Integer.parseInt(o2.get방문자_리뷰수() != null ? o2.get방문자_리뷰수() : "0");
                    return reviewCount2 - reviewCount1; // 내림차순 정렬
                }
            });

            // 마커 추가
            for (EyesData clinic : partneredClinics) {
                addMarkerAndLog(clinic, color);
            }
            for (EyesData clinic : generalClinics) {
                addMarkerAndLog(clinic, color);
            }

            // 리스트뷰에 데이터 업데이트
            List<EyesData> finalList = new ArrayList<>();
            finalList.addAll(partneredClinics);
            finalList.addAll(generalClinics);
            listAdapter.updateHospitals(finalList);

        } catch (Exception e) {
            Log.e("PartnershipActivity", "Error reading JSON file", e);
        }
    }

    private void addMarkerAndLog(EyesData clinic, float color) {
        LatLng clinicLatLng = new LatLng(clinic.get위도(), clinic.get경도());

        // 방문자 리뷰 수가 null이면 기본값 설정
        if (clinic.get방문자_리뷰수() == null) {
            clinic.set방문자_리뷰수("0");
        }

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(clinicLatLng)
                .title(clinic.get이름())
                .snippet(clinic.get도로명주소())
                .icon(BitmapDescriptorFactory.defaultMarker(color)));
        markerEyesMap.put(marker, clinic); // Marker와 병원 정보를 매핑
        Log.d("PartnershipActivity", "Marker added: " + clinic.get이름() + " at " + clinicLatLng.toString());
        Log.d("PartnershipActivity", "Visitor Reviews: " + clinic.get방문자_리뷰수());
    }

    private List<EyesData> loadJhospitals() {
        List<EyesData> jhospitals = new ArrayList<>();
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.jhospitals);
            String json = new java.util.Scanner(inputStream).useDelimiter("\\A").next();

            Gson gson = new Gson();
            Type listType = new TypeToken<List<EyesData>>() {}.getType();
            jhospitals = gson.fromJson(json, listType);

            // jhospitals 리스트의 병원에 flag 추가
            for (EyesData hospital : jhospitals) {
                hospital.setPartnered(true); // 제휴 병원으로 표시
                Log.d("PartnershipActivity", "Jhospitals: " + hospital.get이름() + ", Visitor Reviews: " + hospital.get방문자_리뷰수());
            }
        } catch (Exception e) {
            Log.e("PartnershipActivity", "Error reading jhospitals JSON file", e);
        }
        return jhospitals;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getLocationPermission();
        updateLocationUI();

        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                try {
                    EyesData eyeClinic = markerEyesMap.get(marker);
                    if (eyeClinic != null) {
                        hospitalName = findViewById(R.id.hospital_name);
                        //                     hospitalAddress = findViewById(R.id.hospital_address);
                        hospitalPhone = findViewById(R.id.hospital_phone);
                        hospitalHours = findViewById(R.id.hospital_hours);
                        hospitalCategory = findViewById(R.id.hospital_category);

                        hospitalName.setText(eyeClinic.get이름() != null ? eyeClinic.get이름() : "N/A");
                        hospitalAddress.setText(eyeClinic.get도로명주소() != null ? eyeClinic.get도로명주소() : "N/A");
                        hospitalHours.setText(eyeClinic.get영업시간() != null ? eyeClinic.get영업시간() : "N/A");
                        hospitalPhone.setText(eyeClinic.get일반전화() != null ? eyeClinic.get일반전화() : "N/A");
                        hospitalCategory.setText(eyeClinic.get카테고리() != null ? eyeClinic.get카테고리() : "N/A");

                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    } else {
                        Log.e("PartnershipActivity", "Marker data is null");
                    }
                } catch (Exception e) {
                    Log.e("PartnershipActivity", "Error handling marker click", e);
                }
                return false;
            }
        });

        // 기본적으로 마커를 로드
        loadMarkersAndHospitals(R.raw.jhospitals, BitmapDescriptorFactory.HUE_GREEN);
        init();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            updateLocationUI();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                getDeviceLocation();
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void updateLocationOnMap(Location location) {
        if (location != null) {
            currentLocation = location; // 현재 위치 업데이트
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (firstLocationUpdate) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                firstLocationUpdate = false;
            }
            mMap.addMarker(new MarkerOptions()
                    .position(currentLatLng)
                    .title("현재 위치")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        } else {
            Log.d("PartnershipActivity", "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(0, 0), 15));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateLocationUI();
                getDeviceLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void mCurrentLocation(View view) {
        firstLocationUpdate = true;
        getDeviceLocation();
    }

    // 액티비티 시작 시, 전달 값에 따른 마커표시를 위한 메소드
    private void init() {
        switch (startValue) {
            case 0:
                Toast.makeText(getApplication(), "제휴병원을 초록색으로 표시합니다.", Toast.LENGTH_SHORT).show();
                break;

            case 1:
                // 안과
                currentCategory = "eye_clinic";
                toggleBottomSheet();
                Toast.makeText(getApplication(), "안과를 파란색으로 표시합니다.", Toast.LENGTH_SHORT).show();
                loadMarkersAndHospitals(R.raw.eyes, BitmapDescriptorFactory.HUE_BLUE);
                break;

            case 2:
                // 피부과
                currentCategory = "dermatology";
                toggleBottomSheet();
                Toast.makeText(getApplication(), "피부과를 빨간색으로 표시합니다.", Toast.LENGTH_SHORT).show();
                loadMarkersAndHospitals(R.raw.skin, BitmapDescriptorFactory.HUE_ROSE);
                break;

            case 3:
                // 치과
                currentCategory = "dentist";
                toggleBottomSheet();
                Toast.makeText(getApplication(), "치과를 주황색으로 표시합니다.", Toast.LENGTH_SHORT).show();
                loadMarkersAndHospitals(R.raw.teeth, BitmapDescriptorFactory.HUE_ORANGE);
                break;

            default:
                break;
        }
    }
}