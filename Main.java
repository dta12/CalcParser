import java.util.*;
public class Main {
    public static void main(String[] args) {
        Poly res = new Poly();
        Poly rem = new Poly("0");
        Scanner scan = new Scanner(System.in);
        System.out.println("enter your first polynomial expression: ");
        String exp = scan.nextLine();
        Poly p = new Poly(exp);
        System.out.println("enter your second polynomial expression: ");
        String exp2 = scan.nextLine();
        Poly p2 = new Poly(exp2);
        System.out.println("enter your operation (*, /, -, or +)");
        String oper = scan.nextLine();
        if(oper.equals("*")){
            res = p.multiplyPoly(p2);
        }else if(oper.equals("/")){
            Poly[] div = p.dividePoly(p2);
            res = div[0];
            rem = div[1];
        }else if(oper.equals("-")){
            res = p.subtractPoly(p2);
        }else if(oper.equals("+")){
            res = p.addPoly(p2);
        }
        if(!rem.isZero()){
            System.out.println("result: " + res.polyToString() + ", remainder: " + rem.polyToString());
        }else{
            System.out.println("result: " + res.polyToString());
        }
        scan.close();
    }
}
