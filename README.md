# 카카오칼로리

카카오API, 구글핏API를 이용한 활동 케어 어플리케이션 
- 구글핏API를 이용하여 앱이 실행중이지 않더라도 기기에서 걸음수, 이동거리, 칼로리소모값등을 수집하여 사용자에게 제공. 
- 수집된 데이터는 MPAndroidChart API를 이용해 일/주/월별 데이터를 제공. 
- 카카오API를 이용하여 로그인한 사용자의 정보 및 친구목록을 제공받아 앱을 사용중인 친구들과 활동데이터를 비교하여 순위표 제공(카카오 API검수신청 후 승인시 실제 자신의 카카오톡 친구들 목록 및 데이터를 받아올 수 있음, 현재는 카카오 개발자 사이트에서 테스트용으로 제공되는 기능으로 구현).

- Front : Android(JAVA)
- Back : server - Node.js(express) ,database - mysql  -> https://github.com/wnsvy223/kakaoCalorie_node_server
- openAPI : Google Fitness, 카카오API(카카오로그인, 카카오톡 친구 정보), MPAndroidChart, Firebase Job Dispatcher

  
<div>
<img width="200" src="https://user-images.githubusercontent.com/28755528/50752489-d71bec00-1291-11e9-93ac-93c221346e8f.jpg"></img>
<img width="200" src="https://user-images.githubusercontent.com/28755528/50752625-3e39a080-1292-11e9-8345-c9759b4504ee.jpg"></img>
<img width="200" src="https://user-images.githubusercontent.com/28755528/50752644-4eea1680-1292-11e9-9b8f-53b3070b3e35.jpg"></img>
<img width="200" src="https://user-images.githubusercontent.com/28755528/50752678-6f19d580-1292-11e9-841f-f475a3c142b2.jpg"></img>
<img width="200" src="https://user-images.githubusercontent.com/28755528/50752664-5f9a8c80-1292-11e9-966e-d7a7311626b4.jpg"></img>
<img width="200" src="https://user-images.githubusercontent.com/28755528/50752693-7c36c480-1292-11e9-963f-1cbf761cf88a.jpg"></img>
<img width="200" src="https://user-images.githubusercontent.com/28755528/50752697-7e991e80-1292-11e9-990e-cc4aa2c9dc2b.jpg"></img>
<img width="200" src="https://user-images.githubusercontent.com/28755528/50752699-8062e200-1292-11e9-8764-00c6924b2fea.jpg"></img>
<img width="200" src="https://user-images.githubusercontent.com/28755528/50752705-81940f00-1292-11e9-8b62-7309ca30509c.jpg"></img>
<div>
