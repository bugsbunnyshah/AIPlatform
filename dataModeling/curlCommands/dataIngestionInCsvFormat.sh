curl --location --request POST 'http://localhost:8080/dataMapper/mapCsv' \
--header 'Principal: PAlDekAoo0XWjAicU9SQDKgy7B0y2p2t' \
--header 'Bearer: eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlNlV1JQRlJraWZSNWpwMndOSFliSCJ9.eyJpc3MiOiJodHRwczovL2FwcGdhbGxhYnMudXMuYXV0aDAuY29tLyIsInN1YiI6IlBBbERla0FvbzBYV2pBaWNVOVNRREtneTdCMHkycDJ0QGNsaWVudHMiLCJhdWQiOiJodHRwczovL2FwcGdhbGxhYnMudXMuYXV0aDAuY29tL2FwaS92Mi8iLCJpYXQiOjE2MDEzMjY5MzIsImV4cCI6MTYwMTQxMzMzMiwiYXpwIjoiUEFsRGVrQW9vMFhXakFpY1U5U1FES2d5N0IweTJwMnQiLCJzY29wZSI6InJlYWQ6Y2xpZW50X2dyYW50cyBjcmVhdGU6Y2xpZW50X2dyYW50cyBkZWxldGU6Y2xpZW50X2dyYW50cyIsImd0eSI6ImNsaWVudC1jcmVkZW50aWFscyJ9.eGSMpm1P2rSFFP6s0x4_csKrDSSd8PTko-hHyETSILt9bB6Q0y7u8ky6yOl1piG9RBd5LW7Hy_R_T0bWJjRGEmZ7yUYkaIKafw4oTDpvPOC9T6CeJHcYgaCuroOMSFQhmk9LfZflnl4ODCt21yr4WrI8Teeh8YK5jZ6o8gsk-XrtKISEp04c0GHzBzaMvd5dmBCzSAhFifq3IzvJKkSL5WdXaAsdPgP_BVm5vTYMwTPEm05Cd6E-5S3pPykLO7APKk8s1kLeXSvXnAPkX6y1pbCfZz7dUvHz-fLgMMhx2PUkS_8wM3N2wSZ7rni6MZ3TM7kmqgQy_9SPtOnWSuZfZg' \
--header 'Content-Type: application/json' \
--data-raw '{
  "sourceSchema": "sourceSchema",
  "destinationSchema": "destinationSchema",
  "sourceData": "SCENE_ID,PRODUCT_ID,SPACECRAFT_ID,SENSOR_ID,DATE_ACQUIRED,COLLECTION_NUMBER,COLLECTION_CATEGORY,SENSING_TIME,DATA_TYPE,WRS_PATH,WRS_ROW,CLOUD_COVER,NORTH_LAT,SOUTH_LAT,WEST_LON,EAST_LON,TOTAL_SIZE,BASE_URL\nLE71190612003309ASN03,LE07_L1GT_119061_20031105_20170123_01_T2,LANDSAT_7,ETM,2003-11-05,01,T2,2003-11-05T02:28:44.7529115Z,L1GT,119,61,77.0,-0.48525,-2.39101,111.70726,113.81415,226404871,gs://gcp-public-data-landsat/LE07/01/119/061/LE07_L1GT_119061_20031105_20170123_01_T2\nLM50500191985071PAC01,LM05_L1TP_050019_19850312_20180405_01_T2,LANDSAT_5,MSS,1985-03-12,01,T2,1985-03-12T18:47:36.0820000Z,L1TP,50,19,35.0,59.7621,57.61551,-124.67089,-120.38975,35698890,gs://gcp-public-data-landsat/LM05/01/050/019/LM05_L1TP_050019_19850312_20180405_01_T2\nLT50010902004037COA00,LT05_L1TP_001090_20040206_20161202_01_T1,LANDSAT_5,TM,2004-02-06,01,T1,2004-02-06T14:21:32.0150630Z,L1TP,1,90,61.0,-42.24178,-44.20036,-76.86444,-73.82359,142106156,gs://gcp-public-data-landsat/LT05/01/001/090/LT05_L1TP_001090_20040206_20161202_01_T1\nLT52210741994142CUB00,LT05_L1TP_221074_19940522_20170115_01_T1,LANDSAT_5,TM,1994-05-22,01,T1,1994-05-22T12:35:19.4530630Z,L1TP,221,74,18.0,-19.24433,-21.1886,-50.08834,-47.79272,117777080,gs://gcp-public-data-landsat/LT05/01/221/074/LT05_L1TP_221074_19940522_20170115_01_T1\nLM21240501976112AAA04,LM02_L1TP_124050_19760421_20180424_01_T2,LANDSAT_2,MSS,1976-04-21,01,T2,1976-04-21T01:29:41.5000000Z,L1TP,124,50,5.0,15.54541,13.58551,120.45423,122.56497,27690636,gs://gcp-public-data-landsat/LM02/01/124/050/LM02_L1TP_124050_19760421_20180424_01_T2"
}'