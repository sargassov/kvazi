import server.SecondTaskClass;

public class SecondTaskTesting {
    private static SecondTaskClass secondTaskClass;

    @BeforeClass
    public static void initTest(){
        secondTaskClass = new SecondTaskClass();
        System.out.println("init suite");
    }

    @AfterClass
    public static void destroy(){
        secondTaskClass = null;
    }

}
