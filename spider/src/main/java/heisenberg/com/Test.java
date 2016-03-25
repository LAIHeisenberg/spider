package heisenberg.com;

/**
 * Created by YinYueTai-DEV on 2016/3/23.
 */
public class Test {
    public static void main(String [] args){
        for (int i = -3;i<5;i++) {
            try {

                System.out.println("start..." + i);
                int a = 1 / i;
                System.out.println("end..." + i);


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("finally..." +i);
            }
        }
    }
}
