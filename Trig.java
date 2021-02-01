import java.util.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Trig {
    //fields used in other classes
    public ArrayList<String> terms = new ArrayList<String>();
    ////fields used in this class
    private ArrayList<Double> funcPowers = new ArrayList<Double>();
    private ArrayList<String> arguments = new ArrayList<String>();
    private ArrayList<String> functions = new ArrayList<String>();
    private String res = "";
    //"#" instead of the function arguments in expression String
    private String expression = "";
    Trig(String nonPolyExp){
        String nonPoly = new String(nonPolyExp);
        Pattern nestedParenthRegex = Pattern.compile("(?s)(?=\\()(?:(?=.*?\\((?!.*?\\1)(.*\\)(?!.*\\2).*))(?=.*?\\)(?!.*?\\2)(.*)).)+?.*?(?=\\1)[^(]*(?=\\2$)");
        Matcher matcher = nestedParenthRegex.matcher(nonPoly);
        while(matcher.find()){
            arguments.add(matcher.group(0));
        }
        for(int i = 0; i < arguments.size(); i++){
            if(arguments.get(i).charAt(0) == '(' && arguments.get(i).charAt(arguments.get(i).length() - 1) == ')'){
                arguments.set(i, arguments.get(i).substring(1, arguments.get(i).length() - 1));
            }
        }
        nonPoly = nonPoly.replaceAll("(?s)(?=\\()(?:(?=.*?\\((?!.*?\\1)(.*\\)(?!.*\\2).*))(?=.*?\\)(?!.*?\\2)(.*)).)+?.*?(?=\\1)[^(]*(?=\\2$)", "(#)");
        expression = new String(nonPoly);
        Pattern funcPattern = Pattern.compile("(sinh|cosh|tanh|sin|cos|tan|cot|csc|sec)+\\(#\\)(\\^[0-9]+\\.?[0-9]+?)?");
        Matcher match = funcPattern.matcher(nonPoly);
        while(match.find()){
            if(match.group(0).contains("^")){
                functions.add(match.group(0).split("\\^")[0]);
                funcPowers.add(Double.valueOf(match.group(0).split("\\^")[1]));
                expression = expression.replace(match.group(0), match.group(0).replaceAll("\\^[0-9]+(\\.[0-9]+)?", "@@")); 
            }else{
                functions.add(match.group(0));
                funcPowers.add(Double.valueOf(1));
            }
        }
        for(int i = 0; i < functions.size(); i++){
            functions.set(i, functions.get(i).replace("(#)", ""));
        }
        terms = evalTrig(expression);
        res = setRes();
    }
    //must use "#" instead of functions args in exp
    Trig(String exp, ArrayList<String> funcArguments){
        expression = exp;
        arguments = funcArguments;
        Pattern funcPattern = Pattern.compile("(sinh|cosh|tanh|sin|cos|tan|cot|csc|sec)+\\(#\\)(\\^[0-9]+\\.?[0-9]+?)?");
        Matcher match = funcPattern.matcher(expression);
        while(match.find()){
            if(match.group(0).contains("^")){
                functions.add(match.group(0).split("\\^")[0]);
                funcPowers.add(Double.valueOf(match.group(0).split("\\^")[1]));
                expression = expression.replace(match.group(0), match.group(0).replaceAll("\\^[0-9]+(\\.[0-9]+)?", "@@"));
            }else{
                functions.add(match.group(0));
                funcPowers.add(Double.valueOf(1));
            }
        }
        for(int i = 0; i < functions.size(); i++){
            functions.set(i, functions.get(i).replace("(#)", ""));
        }
        terms = evalTrig(expression);
        res = setRes();
    }
    public ArrayList<Double> getFuncPowers(){
        return funcPowers;
    }
    public String simplifyTrigTerm(String trm){
        String t = new String(trm);
        ArrayList<Poly> mixedTerm = new ArrayList<Poly>();
        StringBuilder res = new StringBuilder();
        Pattern polTerms = Pattern.compile("[-+]?([0-9]+(\\.[0-9])?x?)((?<=x)(\\^[0-9]+(\\.[0-9]+)?))?");
        Matcher m = polTerms.matcher(t);
        while(m.find()){
            if(!m.group(0).isEmpty()){
                mixedTerm.add(new Poly(m.group(0)));
                t = t.replace(m.group(0), "");
            }
        }
        Poly p = new Poly("1");
        for(int i = 0; i < mixedTerm.size(); i++){
            p = p.multiplyPoly(mixedTerm.get(i));
        }
        res.append(p.polyToString()).append(t); 
        p = new Poly("1");
        return res.toString();
    }
    //full expression w "#" as args
    public ArrayList<String> evalTrig(String fullExpression){
        ArrayList<String> result = new ArrayList<String>();
        String[] termsT = fullExpression.split("\\+");
        for(int i = 0; i < termsT.length; i++){
            StringBuilder resT = new StringBuilder("");
            termsT[i] = simplifyTrigTerm(termsT[i]);
            StringBuilder polyTs = new StringBuilder();
            Pattern nonTrig = Pattern.compile("[-+]?([0-9]+(\\.[0-9])?x?)((?<=x)(\\^[0-9]+(\\.[0-9]+)?))?");
            Matcher match = nonTrig.matcher(termsT[i]);
            while(match.find()){
                if(!match.group(0).isEmpty()){
                    polyTs.append(match.group(0));
                    termsT[i] = termsT[i].replace(match.group(0), "");
                }
            }
            Poly pTerms = new Poly(polyTs.toString());
            if(polyTs.isEmpty() || Pattern.matches("0+", polyTs)){
                pTerms = new Poly("1");
            }
            String nonpTerms = "";
            for(int j = 0; j < functions.size(); j++){
                if(termsT[i].contains(functions.get(j))){
                    Poly funcArgs = new Poly(arguments.get(j));
                    if(funcArgs.getDegree().equals(0.0) && functions.get(j).equals("sin")){
                        pTerms = pTerms.multiplyConst(Math.pow(Math.sin(Double.parseDouble(arguments.get(j))), this.funcPowers.get(j)));
                        termsT[i] = termsT[i].replaceFirst("sin\\(#\\)", "");
                        functions.set(j, "done");
                        arguments.set(j, "computed");
                    }else if(funcArgs.getDegree().equals(0.0) && functions.get(j).equals("cos")){
                        pTerms = pTerms.multiplyConst(Math.pow(Math.cos(Double.parseDouble(arguments.get(j))), this.funcPowers.get(j)));
                        termsT[i] = termsT[i].replaceFirst("cos\\(#\\)", "");
                        functions.set(j, "done");
                        arguments.set(j, "computed");
                    }else if(funcArgs.getDegree().equals(0.0) && functions.get(j).equals("tan")){
                        pTerms = pTerms.multiplyConst(Math.pow(Math.tan(Double.parseDouble(arguments.get(j))), this.funcPowers.get(j)));
                        termsT[i] = termsT[i].replaceFirst("tan\\(#\\)", "");
                        functions.set(j, "done");
                        arguments.set(j, "computed");
                    }else if(funcArgs.getDegree().equals(0.0) && functions.get(j).equals("sinh")){
                        pTerms = pTerms.multiplyConst(Math.pow(Math.sinh(Double.parseDouble(arguments.get(j))), this.funcPowers.get(j)));
                        termsT[i] = termsT[i].replaceFirst("sinh\\(#\\)", "");
                        functions.set(j, "done");
                        arguments.set(j, "computed");
                    }else if(funcArgs.getDegree().equals(0.0) && functions.get(j).equals("cosh")){
                        pTerms = pTerms.multiplyConst(Math.pow(Math.cosh(Double.parseDouble(arguments.get(j))), this.funcPowers.get(j)));
                        termsT[i] = termsT[i].replaceFirst("cosh\\(#\\)", "");
                        functions.set(j, "done");
                        arguments.set(j, "computed");
                    }else if(funcArgs.getDegree().equals(0.0) && functions.get(j).equals("tanh")){
                        pTerms = pTerms.multiplyConst(Math.pow(Math.tanh(Double.parseDouble(arguments.get(j))), this.funcPowers.get(j)));
                        termsT[i] = termsT[i].replaceFirst("tanh\\(#\\)", "");
                        functions.set(j, "done");
                        arguments.set(j, "computed");
                    }else if(funcArgs.getDegree().equals(0.0) && functions.get(j).equals("csc")){
                        pTerms = pTerms.multiplyConst(Math.pow(1/Math.sin(Double.parseDouble(arguments.get(j))), this.funcPowers.get(j)));
                        termsT[i] = termsT[i].replaceFirst("csc\\(#\\)", "");
                        functions.set(j, "done");
                        arguments.set(j, "computed");
                    }else if(funcArgs.getDegree().equals(0.0) && functions.get(j).equals("sec")){
                        pTerms = pTerms.multiplyConst(Math.pow(1/Math.cos(Double.parseDouble(arguments.get(j))), this.funcPowers.get(j)));
                        termsT[i] = termsT[i].replaceFirst("sec\\(#\\)", "");
                        functions.set(j, "done"); 
                        arguments.set(j, "computed");
                    }else if(funcArgs.getDegree().equals(0.0) && functions.get(j).equals("cot")){
                        pTerms = pTerms.multiplyConst(Math.pow(1/Math.tan(Double.parseDouble(arguments.get(j))), this.funcPowers.get(j)));
                        termsT[i] = termsT[i].replaceFirst("cot\\(#\\)", "");
                        functions.set(j, "done");
                        arguments.set(j, "computed");
                    }else if(funcArgs.getDegree() > 0){
                        termsT[i] = termsT[i].replaceFirst(functions.get(j) + "\\(#\\)", "");
                        nonpTerms = nonpTerms + functions.get(j) + "(" + arguments.get(j) + ")" + "^" + funcPowers.get(j);
                        functions.set(j, "ptfunc@" + functions.get(j));
                        arguments.set(j, "ptarg@" + arguments.get(j)); 
                    }
                }
            }
            result.add(resT.append(pTerms.polyToString()).append(nonpTerms).toString());
        }
        return result;
    }
    public Trig multiplyTrig(Trig t2){
        StringBuilder resTerms = new StringBuilder();
        ArrayList<StringBuilder> resTermsArray = new ArrayList<StringBuilder>();
        ArrayList<String> trig1 = new ArrayList<String>(this.getTerms());
        ArrayList<String> trig2 = new ArrayList<String>(t2.getTerms());
        for(int i = 0; i < trig1.size(); i++){ 
            Poly resPoly = new Poly("1");
            String noArgs1 = new String(argstoNumSign(trig1.get(i)));
            StringBuilder[] poly0trig1 = seperate(noArgs1);
            Poly trigPoly1 = new Poly(poly0trig1[0].toString());
            Trig trigObj1 = new Trig(noArgs1, extractArgs(trig1.get(i)));
            for(int j = 0; j < trig2.size(); j++){ 
                String noArgs2 = new String(argstoNumSign(trig2.get(j)));
                StringBuilder[] poly0trig12 = seperate(noArgs2);
                Poly trigPoly2 = new Poly(poly0trig12[0].toString());
                resPoly = trigPoly1.multiplyPoly(trigPoly2);
                Trig trigObj2 = new Trig(noArgs2, extractArgs(trig2.get(j)));
                Trig resTerm = multiply2Terms(trigObj1, trigObj2);
                
                StringBuilder overall = new StringBuilder(resPoly.polyToString());
                overall.append(resTerm.getTerms().get(0).replaceAll("(?<!\\^)(1\\.0)", ""));
                resTermsArray.add(overall);
            }    
        }
        for(int i = 0; i < resTermsArray.size(); i++){
            resTerms.append(resTermsArray.get(i)).append("+");
            if(i == resTermsArray.size() - 1){
                resTerms = new StringBuilder(resTerms.substring(0, resTerms.length() - 1));
            }
        }
        return new Trig(resTerms.toString());
    }
    public Trig multiply2Terms(Trig a, Trig b){
        StringBuilder resStr = new StringBuilder("");
        for(int i = 0; i < b.getFunctions().size(); i++){
            b.getFunctions().set(i, b.getFunctions().get(i).replace("ptfunc@", ""));
            b.getArgs().set(i, b.getArgs().get(i).replace("ptarg@", ""));
        }
        for(int i = 0; i < a.getFunctions().size(); i++){
            a.getFunctions().set(i, a.getFunctions().get(i).replace("ptfunc@", ""));
            a.getArgs().set(i, a.getArgs().get(i).replace("ptarg@", ""));
            if(b.getFunctions().contains(a.getFunctions().get(i))){
                for(int j = 0; j < b.getFunctions().size(); j++){
                    if(a.getFunctions().contains(b.getFunctions().get(j))){
                        if(b.getFunctions().get(j).equals(a.getFunctions().get(i)) && b.getArgs().get(j).equals(a.getArgs().get(i))){
                            Double pow = Double.sum(a.getFuncPowers().get(i), b.getFuncPowers().get(j));
                            resStr.append(b.getFunctions().get(j) + "(" + b.getArgs().get(j) + ")^" + pow);
                            b.getFunctions().set(j, "element");
                        }else if(b.getFunctions().get(j).equals(a.getFunctions().get(i)) && !b.getArgs().get(j).equals(a.getArgs().get(i))){
                            resStr.append(a.getFunctions().get(i) + "(" + a.getArgs().get(i) + ")^" + a.getFuncPowers().get(i));
                            resStr.append(b.getFunctions().get(j) + "(" + b.getArgs().get(j) + ")^" + b.getFuncPowers().get(j));
                            b.getFunctions().set(j, "element");
                        }
                    }
                }
            }else{
                resStr.append(a.getFunctions().get(i) + "(" + a.getArgs().get(i) + ")^" + a.getFuncPowers().get(i));
                for(int j = 0; j < b.getFunctions().size(); j++){
                    if(!b.getFunctions().get(j).equals("element")){
                        resStr.append(b.getFunctions().get(j) + "(" + b.getArgs().get(j) + ")^" + b.getFuncPowers().get(j));
                        b.getFunctions().set(j, "element");
                    }
                }
            }
        }
        return new Trig(resStr.toString());
    }
    public String setRes(){
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < terms.size(); i++){
            if(i == terms.size() - 1){
                result.append(terms.get(i));
            }else{
                result.append(terms.get(i)).append("+");
            }
        }
        return result.toString();
    }
    public ArrayList<String> getArgs(){
        return arguments;
    }
    public String getRes(){
        return res;
    }
    public ArrayList<String> getFunctions(){
        return functions;
    }
    public ArrayList<String> getTerms(){
        return terms;
    }
    public ArrayList<String> extractArgs(String s){
        ArrayList<String> res = new ArrayList<>();
        Pattern nestedParenthRegex = Pattern.compile("(?s)(?=\\()(?:(?=.*?\\((?!.*?\\1)(.*\\)(?!.*\\2).*))(?=.*?\\)(?!.*?\\2)(.*)).)+?.*?(?=\\1)[^(]*(?=\\2$)");
        Matcher matcher = nestedParenthRegex.matcher(s);
        while(matcher.find()){
            res.add(matcher.group(0));
        }
        for(int i = 0; i < res.size(); i++){
            if(res.get(i).charAt(0) == '(' && res.get(i).charAt(res.get(i).length() - 1) == ')'){
                res.set(i, res.get(i).substring(1, res.get(i).length() - 1));
            }
        }
        return res;
    }
    public StringBuilder[] seperate(String in){
        String s = new String(in);
        StringBuilder[] poly0trig1 = new StringBuilder[2];
        poly0trig1[0] = new StringBuilder("");
        poly0trig1[1] = new StringBuilder("");
        Matcher m = Pattern.compile("(?<!\\^)([-+]?[0-9]*\\.?[0-9]+)?(x(?<=x)(\\^[0-9]+(\\.[0-9]+)?)?)?").matcher(s);
        while(m.find()){
            if(!m.group(0).isEmpty() && !Pattern.matches("\\.[0-9]+", m.group(0))){
                poly0trig1[0].append(m.group(0));
                poly0trig1[1].append(s.replaceFirst(m.group(0), ""));
            } 
        }
        if(poly0trig1[0].isEmpty() || poly0trig1[0].equals(null) ){
            poly0trig1[0].append("1");
            poly0trig1[1].append(s);
        }
        return poly0trig1;
    }
    public String argstoNumSign(String inS){
        String s = new String(inS);
        return s.replaceAll("(?s)(?=\\()(?:(?=.*?\\((?!.*?\\1)(.*\\)(?!.*\\2).*))(?=.*?\\)(?!.*?\\2)(.*)).)+?.*?(?=\\1)[^(]*(?=\\2$)", "(#)");
    }
    public int trigCounter(String inTerm){
        int counter = 0;
        Pattern p = Pattern.compile("sin|cos|tan|csc|sec|cot|sinh|cosh|tanh");
        Matcher m = p.matcher(inTerm);
        while(m.find()){
            counter++;
        }
        return counter;
    }
    public boolean equals(Trig t){
        ArrayList<String> tts = new ArrayList<>();
        ArrayList<String> tts2 = new ArrayList<>();
        for(int i = 0; i < this.getFunctions().size(); i++){
            tts.add(this.getFunctions().get(i) + "(" + this.getArgs().get(i) + ")^" + this.getFuncPowers().get(i));
        }
        for(int j = 0; j < t.getFunctions().size(); j++){
            tts2.add(t.getFunctions().get(j) + "(" + t.getArgs().get(j) + ")^" + t.getFuncPowers().get(j));
        } 
        if ((tts == null && tts2 != null) || (tts != null && tts2 == null) || (tts.size() != tts2.size())) {
            return false;
        }
        tts = new ArrayList<String>(tts); 
        tts2 = new ArrayList<String>(tts2);   
        Collections.sort(tts);
        Collections.sort(tts2);      
        return tts.equals(tts2);
    }
    public Trig simplifyTrig(){
        ArrayList<String> resTerms = new ArrayList<String>(this.getTerms());
        StringBuilder res = new StringBuilder();
        for(int i = 0; i < this.getTerms().size(); i++){
            String noArgs1 = argstoNumSign(this.getTerms().get(i));
            StringBuilder[] sep1 = seperate(noArgs1);
            Poly poly1 = new Poly(sep1[0].toString());
            Trig trig1 = new Trig(sep1[1].toString(), extractArgs(this.getTerms().get(i)));
            for(int j = 0; j < this.getTerms().size(); j++){
                if(j == i){
                    continue;
                }
                String noArgs2 = argstoNumSign(this.getTerms().get(j));
                StringBuilder[] sep2 = seperate(noArgs2);
                Poly poly2 = new Poly(sep2[0].toString());
                Trig trig2 = new Trig(sep2[1].toString(), extractArgs(this.getTerms().get(j)));
                if(poly1.getDegree().equals(poly2.getDegree()) && trig1.equals(trig2)){
                    resTerms.set(i, poly1.addPoly(poly2).polyToString() + trig1.getTerms().get(0).replaceAll("(?<!\\^)(1\\.0)", ""));
                    resTerms.set(j, "");
                }
            }
        }
        for(int i = 0; i < resTerms.size(); i++){
            if(!resTerms.get(i).isEmpty()){
                res.append(resTerms.get(i)).append("+");
            }
        }
        if(res.charAt(res.length() - 1) == '+'){
            res = new StringBuilder(res.substring(0, res.length() - 1));
        }
        return new Trig(res.toString());
    }
}
