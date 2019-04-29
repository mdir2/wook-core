# wook-core
DI, MVC 패턴을 구현한 경량 프레임워크입니다.

  DI : web.xml의 context와 properties 파일을 통해 설정 정보를 읽어 들이고 프로젝트 pakage 내의 자바파일을 검색합니다. 
  그 후 리플렉션을 활용해 동적으로 객체를 생성하고 LinkedHashMap에 객체를 저장하고 어노테이션을 통해 객체를 주입합니다.
  
  MVC : HttpServlet을 상속받아 doGet과 doPost을 수행할때 요청받은 주소를 통해 해당 컨트롤러의 메소드를 특정하고 리플렉션을 이용하여 메소드를 동적으로 수행시킵니다. 
  그런 후에는 설정파일에 정해놓은 view저장 폴더의 jsp를 찾아 forward합니다.
