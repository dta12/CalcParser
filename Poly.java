//package sample.Parser;
import java.util.*;
import java.lang.String;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Poly {
    //fields used in other classes
    public String nonPolyExp = "";
    public ArrayList<String> nonPolyArgs = new ArrayList<>();
    //fields used in this class
    private Double degree = Double.valueOf(-1);
    private ArrayList<Double> powers;
    private ArrayList<Double> coefficients;
    private String nonPolyRes = "";
    public Poly(){
        this("0");
    }
    public Poly(String polynomial){
        polynomial = polynomial.replaceAll("\\s", "");
        if(polynomial.charAt(0) == 'x'){
            polynomial = "1" + polynomial;
        }
        coefficients = setCoefficients(polynomial);
        powers = setPowers(polynomial);
        for(Double pow : powers){
            if(pow > degree){
                degree = pow;
            }
        }
        for(int i = 0; i < coefficients.size(); i++){
            if(coefficients.get(i).equals(0.0) && !powers.get(i).equals(0.0)){
                powers.set(i, Double.valueOf(0));
                coefficients.set(i, Double.valueOf(0));
            }
        }
        if(!nonPolyArgs.isEmpty()){
            Trig nonPoly = new Trig( nonPolyExp , nonPolyArgs);
            for(int i = 0; i < nonPoly.getTerms().size(); i++){
                int counter = nonPoly.getTerms().size();
                while(counter != 0 && Pattern.matches("[-+]?[0-9]*\\.?[0-9]*+x?(\\^[0-9]+\\.?[0-9]?)?", nonPoly.getTerms().get(i))){
                    Poly compTerm = new Poly(nonPoly.getTerms().get(i));
                    powers.add(compTerm.getPowers().get(0));
                    coefficients.add(compTerm.getCoefficients().get(0));
                    nonPoly.terms.remove(nonPoly.getTerms().get(i));
                    counter--;
                }
            }
            nonPolyRes = nonPoly.setRes();
        }
        this.simplify();
    }
    public Poly(ArrayList<Double> constants, ArrayList<Double> degrees){
        powers = degrees;
        coefficients = constants;
        for(int i = 0; i < constants.size(); i++){
            if(coefficients.get(i).equals(0.0) && !powers.get(i).equals(0.0)){
                powers.set(i, Double.valueOf(0));
                coefficients.set(i, Double.valueOf(0));
            }
        }

    }
    public ArrayList<String> setNonPolyArgs(String str){
        ArrayList<String> arguments = new ArrayList<String>();
        StringBuilder s = new StringBuilder(str);
        Pattern nestedParenthRegex = Pattern.compile("(?s)(?=\\()(?:(?=.*?\\((?!.*?\\1)(.*\\)(?!.*\\2).*))(?=.*?\\)(?!.*?\\2)(.*)).)+?.*?(?=\\1)[^(]*(?=\\2$)");
        Matcher matcher = nestedParenthRegex.matcher(s.toString());
        while(matcher.find()){
            arguments.add(matcher.group(0));
        }
        for(int i = 0; i < arguments.size(); i++){
            if(arguments.get(i).charAt(0) == '(' && arguments.get(i).charAt(arguments.get(i).length() - 1) == ')'){
                arguments.set(i, arguments.get(i).substring(1, arguments.get(i).length() - 1));
            }
        }
        return arguments;
    }
    public ArrayList<String> getNonPolyArgs(){
        return nonPolyArgs;
    }
    //the non poly function arguments are stored in the nonPolyArgs ArrayList, the Args in the String expression are replaced with "#"
    public String getNonPolyExp(){
        return nonPolyExp;
    }
    public String polyToString(){
        String resString = new String();
        StringBuilder stringPoly = new StringBuilder();
        for(int i = 0; i < coefficients.size(); i++){
            if(powers.get(i) != 0){
                if(coefficients.get(i) > 0){
                    stringPoly.append("+").append(coefficients.get(i)).append("x^").append(powers.get(i));
                }else if(coefficients.get(i).equals(0.0)){
                    stringPoly.append("0");
                }else{
                    stringPoly.append(coefficients.get(i)).append("x^").append(powers.get(i));
                }
            }else{
                if(coefficients.get(i) > 0){
                    stringPoly.append("+").append(coefficients.get(i));
                }else if(coefficients.get(i).equals(0.0)){
                    stringPoly.append("+").append("0");
                }else{
                    stringPoly.append(coefficients.get(i));
                }
            }
        }
        if(stringPoly.length() != 0 && stringPoly.charAt(0) == '+' || stringPoly.length() != 0 && stringPoly.charAt(0) == 'x'){
            resString = stringPoly.substring(1);
        }else{
            resString = stringPoly.toString();
        }
        if(!nonPolyRes.isEmpty()){
            resString = resString + "+" + nonPolyRes;
        }
        return resString;
    }
    public void setDegree(){
        degree = Collections.max(this.getPowers());
    }
    public Double getDegree(){
        Double res = Double.valueOf(-1);
        if(!powers.isEmpty()){
            res = Collections.max(this.getPowers());
        }
        return res;
    }
    public ArrayList<Double> setCoefficients(String expression){
        ArrayList<Double> values = new ArrayList<Double>();
        nonPolyArgs = this.setNonPolyArgs(expression);
        expression = expression.replaceAll("(?s)(?=\\()(?:(?=.*?\\((?!.*?\\1)(.*\\)(?!.*\\2).*))(?=.*?\\)(?!.*?\\2)(.*)).)+?.*?(?=\\1)[^(]*(?=\\2$)", "(#)");
        if(!expression.equals("0") || !expression.equals("+0") || !expression.equals("-0")){
            expression = expression.replace("-", "+-");
            String[] terms = expression.split("\\+");
            for (int i = 0; i < terms.length; i++) {
                if(Pattern.matches("[-+]?[0-9]*\\.?[0-9]*+x?(?<=x)(\\^[0-9]+\\.?[0-9]?)?", terms[i])){
                    if((i == 0 && expression.indexOf("+-") == 0)){
                        continue;
                    }
                    if(terms.length > 1 &&  terms[1].contains("-x") && expression.indexOf("+-") == 0){
                        terms[1] = terms[1].replace("-x", "-1x");
                    }
                    if(terms.length == 2 && terms[0].isEmpty() && expression.indexOf("+-") != 0){ 
                        terms = new String[]{terms[1]};
                    }
                    String[] termValues = terms[i].split("x"); 
                    if(termValues.length >= 1 && termValues[0].equals("-")){
                        values.add(Double.valueOf(-1));
                    }else if( termValues.length != 0 && termValues[0].equals("-")){ 
                        values.add(Double.valueOf(-1));
                    }else if(termValues.length == 0 || termValues[0].equals("") || termValues[0] == null){
                        values.add(Double.valueOf(1));
                    }else{
                        values.add(Double.valueOf(termValues[0]));
                    }
                }else if(Pattern.matches("[-+]?[0-9]*\\.?[0-9]*", terms[i])){
                    values.add(Double.valueOf(terms[i]));
                }
                else{
                    nonPolyExp = nonPolyExp + terms[i] + "+";
                }
            }
        }else{
            values.add(Double.valueOf(0));
        }
        if( !nonPolyExp.isEmpty() && nonPolyExp.charAt(nonPolyExp.length() - 1) == '+'){
            nonPolyExp = nonPolyExp.substring(0, nonPolyExp.length() - 1);
        }
        nonPolyExp = nonPolyExp.replaceAll("\\+-", "-");
        return values;
    }
    public ArrayList<Double> getCoefficients(){
        return coefficients;
    }
    public ArrayList<Double> setPowers(String expression){
        ArrayList<Double> exponents = new ArrayList<Double>();
        expression = expression.replace("-", "+-");
        String[] terms = expression.split("\\+");   
        for (int i = 0; i < terms.length; i++) {
            if(Pattern.matches("[-+]?[0-9]*\\.?[0-9]*+x?(\\^[0-9]+\\.?[0-9]?)?", terms[i])){
                if(i == 0 && expression.charAt(0) == '+'){
                    continue;
                }
                if(terms[i].contains("x")){
                    String[] termValues = terms[i].split("x"); 
                    if(termValues.length <= 1){
                        exponents.add(Double.valueOf(1));
                    }else{
                        termValues[1] = termValues[1].replace("^", "");
                        if(expression.charAt(0) == '+' && coefficients.get(i-1).equals(0.0)){
                            exponents.add(Double.valueOf(0));
                        }
                        exponents.add(Double.valueOf(termValues[1]));
                    }
                }else{
                    exponents.add(Double.valueOf(0));
                }
            }
        }
        return exponents;
    }
    public ArrayList<Double> getPowers(){
        return powers;
    }
    public void setCoefficientArray(ArrayList<Double> cofs){
        coefficients = cofs;
    }
    public void setPowerArray(ArrayList<Double> pows){
        powers = pows;
    }
    public Poly addTerm(String term){
        Poly t = new Poly(term);
        this.coefficients.add(t.getCoefficients().get(0));
        this.powers.add(t.getPowers().get(0));
        return new Poly(this.coefficients, this.powers);
    }
    public Poly addTerm(Double c, Double p){
        this.coefficients.add(Double.valueOf(c));
        this.powers.add(Double.valueOf(p));
        return new Poly(this.coefficients, this.powers);
    }
    public Poly addTerm(Poly term){
        if(term.coefficients.size() == 1 && term.powers.size() == 1){
            this.coefficients.add(term.getCoefficients().get(0));
            this.powers.add(term.getCoefficients().get(0));
        }
        return new Poly(this.coefficients, this.powers);
    }
    public Poly multiplyTerm(Poly term){
        Poly res = new Poly();
        if(term.getCoefficients().size() == 1 && term.getPowers().size() == 1){
            res = this.multiplyPoly(term);
        }
        return res;
    }
    public Poly divideTerm(Poly term){
        Poly res = new Poly(new ArrayList<Double>(), new ArrayList<Double>());
        if(term.getCoefficients().size() == 1 && term.getPowers().size() == 1){
            for(int i = 0; i < this.getCoefficients().size(); i++){
                res.addTerm(this.getCoefficients().get(i) / term.getCoefficients().get(0), this.getPowers().get(i) - term.getPowers().get(0));
            }
        }
        return res;
    }
    public Poly divideTerm(String t){
        Poly res = this.divideTerm(new Poly(t));
        return res;
    }
    public Poly multiplyTerm(String t){
        Poly res = this.multiplyPoly(new Poly(t));
        return res;
    }
    public Poly getLeadingTerm(){
        this.simplify();
        ArrayList<Double> pow = new ArrayList<Double>();
        ArrayList<Double> cof = new ArrayList<Double>();
        pow.add(this.getDegree());
        cof.add(this.getCoefficients().get(0));
        return new Poly(cof, pow);
    }
    public Poly multiplyPoly(Poly polyTwo){
        ArrayList<Double> uncombinedCoefficients = new ArrayList<Double>();
        ArrayList<Double> uncombinedPowers = new ArrayList<Double>();
        for(int i = 0; i < coefficients.size(); i++){
            for(int j = 0; j < polyTwo.getCoefficients().size(); j++){
                uncombinedCoefficients.add(coefficients.get(i) * polyTwo.getCoefficients().get(j));
                uncombinedPowers.add(powers.get(i) + polyTwo.getPowers().get(j));
            }
        }
        Poly res = new Poly(uncombinedCoefficients, uncombinedPowers);
        res.simplify();
        return res;
    }
    public Poly multiplyConst(Double num){
        Poly res = new Poly(coefficients, powers);
        for(int i = 0; i < res.getCoefficients().size(); i++){
            res.coefficients.set(i, coefficients.get(i) * num);
        }
        res.simplify();
        return res;
    }
    public Poly[] dividePoly(Poly p2){
        Poly d = p2;
        Poly n = new Poly(this.getCoefficients(), this.getPowers());
        Poly q = new Poly();
        Poly r = n;
        while(!r.isZero() && r.getDegree() >= d.getDegree()){
            Poly t = r.getLeadingTerm().divideTerm(d.getLeadingTerm());
            q = q.addPoly(t);
            Poly mid = t.multiplyPoly(d);
            r = r.subtractPoly(mid);
        }
        return new Poly[]{q, r};
    }
    public Poly addPoly(Poly polyTwo){
        Poly res = new Poly("0");
        if(this.getCoefficients().size() <= polyTwo.getCoefficients().size()){
            res.setCoefficientArray(polyTwo.getCoefficients());
            res.setPowerArray(polyTwo.getPowers());
            for(int i = 0; i < res.getCoefficients().size(); i++){
                if(i < getCoefficients().size()){
                        res.coefficients.add(getCoefficients().get(i));
                        res.powers.add(getPowers().get(i));
                }
            }
        }else{
            res.setCoefficientArray(this.getCoefficients()); 
            res.setPowerArray(this.getPowers());
            for(int i = 0; i < res.getCoefficients().size(); i++){
                if(i < polyTwo.getCoefficients().size()){
                        res.coefficients.add(polyTwo.getCoefficients().get(i));
                        res.powers.add(polyTwo.getPowers().get(i));
                }
            }
        }
        res.simplify();
        return res;
    }
    public Poly subtractPoly(Poly polyTwo){
        Poly p1 = new Poly(coefficients, powers);
        Poly p2 = polyTwo.multiplyConst(Double.valueOf(-1));
        Poly res = p1.addPoly(p2);
        return res;
    }
    public void simplify(){

        Set<Double> pows = new HashSet<Double>(this.getPowers());
        ArrayList<Double> sortedPowers = new ArrayList<>(pows);
        sortedPowers.sort(Collections.reverseOrder());
        ArrayList<Double> resCoefs = new ArrayList<Double>(Collections.nCopies(sortedPowers.size(), Double.valueOf(0)));
        for(int i = 0; i < sortedPowers.size(); i++){
            for(int j = 0; j < this.getCoefficients().size(); j++){
                if(this.getPowers().get(j).equals(sortedPowers.get(i))){
                    resCoefs.set(i, resCoefs.get(i) + this.getCoefficients().get(j));
                }
            }
        }
        this.powers = sortedPowers;
        this.coefficients = resCoefs;
    }
    public Poly polyExp(Double pow){
        Poly thisPoly = new Poly(this.getCoefficients(), this.getPowers());
        Poly res = new Poly("1");
        if(pow >= 1.0 ){
            for(int i = 0; i < pow; i++){
                res = res.multiplyPoly(thisPoly);
            }
        }
        return res;
    }
    public boolean isZero(){
        boolean res = true;
        for(int i = 0; i < coefficients.size(); i++){
            if(coefficients.get(i) != 0){
                res = false;
            }
        }
        return res;
    }
    public boolean isPoly(){
        if(nonPolyArgs.isEmpty() && nonPolyExp.isEmpty()){
            return true;
        }
        return false;
    }
    public void printPoly(){
        System.out.println("coefficients: " + coefficients.toString());
        System.out.println("exponents: " + powers.toString());
        System.out.println(this.polyToString());
    }
}
