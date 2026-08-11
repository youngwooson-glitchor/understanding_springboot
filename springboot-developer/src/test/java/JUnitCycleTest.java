import org.junit.jupiter.api.*;


public class JUnitCycleTest {
    @BeforeAll // run once before start a whole test, should declare static
    static void BeforeAll() {
        System.out.println("@BeforeAll");
    }

    @BeforeEach // run every time before test case
    public void beforeEach() {
        System.out.println("@BeforeEach");
    }

    @Test
    public void test1() {
        System.out.println("test1");

    }

    @Test
    public void test2() {
        System.out.println("test2");
    }

    @Test
    public void test3() {
        System.out.println("test3");
    }

    @AfterAll // run once before exit process after running whole test. shoudl declare as static
    static void afterAll() {
        System.out.println("@AfterAll");
    }

    @AfterEach // run every closing test cases
    public void afterEach() {
        System.out.println("@AfterEach");
    }

}
