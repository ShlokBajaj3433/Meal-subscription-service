public class App {
     public static void main(String[] args) throws Exception {
        System.out.println("== Login ==");
        LoginTest.main(args);

        System.out.println("== Register ==");
        RegisterTest.main(args);

        System.out.println("== Meals ==");
        MealsTest.main(args);

        System.out.println("== Dashboard ==");
        DashboardTest.main(args);

        System.out.println("== Admin meals ==");
        AdminMealsTest.main(args);
    }
}
