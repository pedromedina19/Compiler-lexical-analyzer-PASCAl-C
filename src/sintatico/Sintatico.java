package sintatico;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import lexico.Classe;
import lexico.Lexico;
import lexico.Token;
import sintatico.simbolo.Tipo;

// Classe que implementa o analisador sintatico
public class Sintatico {
    // Atributos da classe
    private Lexico lexico;
    private Token token;
    //private Verifica verifica;
    private Scanner scanner;
    private String codigo;
    private FileWriter fw;
    private List<simbolo> variaveis = new ArrayList<>();
    private List<String> parametros = new ArrayList<>();
    private boolean repeat = false;
    

    public Sintatico(Lexico lexico) {
        this.lexico = lexico;
        scanner = new Scanner(System.in);
        try {
            codigo = "#include <stdio.h>\n#include <stdlib.h>\n#include <string.h>\n#include <math.h>\nint main(){\n";
            fw = new FileWriter(new File("CODIGOC.c"));                       
        } catch (IOException e) {
            System.out.println("Não foi possível criar o arquivo de saída.");
            e.printStackTrace();
        }    
    }

    public void analisar() {   
        token = lexico.nextToken();
		try {
            programa();
            fw.close();
            System.out.println("Código em Pascal traduzido com sucesso para a linguagem C."); 
        } catch (IOException e) {
            System.out.println("Não foi possível fechar o arquivo de saida.");
            e.printStackTrace();
        }                         
    }
    // Método para analisar a regra de produção do programa
    private void programa() {
        if (!palReservada(token, "program")) {
            erro("program");
            return;
        }
        token = lexico.nextToken();
        if (token.getClasse() != Classe.cId) {
            erro("Nome do Programa esperado");
            return;
        }
        token = lexico.nextToken();
        if (token.getClasse() != Classe.cPontoVirg) {
            erro("Ponto e vírgula (;) esperado");
            return;
        }
        token = lexico.nextToken();
        corpo();        
        if (token.getClasse() != Classe.cPonto) {
            erro("Ponto Final (.) esperado");
        }
        token = lexico.nextToken();
    }
    // Método para analisar a regra de produção do corpo
    private void corpo() {
        declara();        
        gerarCodigoVariaveis();
        if (palReservada(token, "begin")) {
            token = lexico.nextToken();            
            sentencas();
            codigo += "return 0;\n";
            codigo += "}";
            if (palReservada(token, "end")) {
                token = lexico.nextToken();                  
                gerarCodigo();              
            } else {
                erro("end esperado");
            }
        } else {
            erro("begin esperado");
        }
    }

    private void declara() {
        if (palReservada(token, "var")) {
            token = lexico.nextToken();
            dvar();
            mais_dc();
        }
    }
    
    private void dvar() {
        if(token.getClasse() == Classe.cId){
            //salva as variáveis            
            if(!verificar_Variavel(token.getValor().getTexto()))
                variaveis.add(new simbolo(token.getValor().getTexto(), Tipo.INTEIRO, null));
            else
                erro("Variável já foi declarada.");

            token = lexico.nextToken();        
            if(token.getClasse() == Classe.cVirg){
                token = lexico.nextToken();
                dvar();
            }else if(token.getClasse() == Classe.cDoisPontos){
                token = lexico.nextToken();
                if(palReservada(token, "integer")){
                    token = lexico.nextToken();
                }
            }else{
                erro("Tipo não especificado.");
            }                      
        }else{
            erro("Identificador esperado.");
        }     
    }

    private void mais_dc() {
        if(token.getClasse() == Classe.cPontoVirg){
            token = lexico.nextToken();            
            if(!palReservada(token, "begin")){
                dvar();
                mais_dc();
            }
        }else{
            erro("Ponto e vírgula (;) esperado");
        }      
    }
    
    private void sentencas() {                       
        comando();        
        mais_sentenca();     
    }

    private void mais_sentenca(){                 
        consumirToken(Classe.cPontoVirg); 
        codigo += ";\n"; 
        if(!palReservada(token, "end") && !palReservada(token, "until"))
            sentencas();       
    }

    private void comando() {         
        if (token.getClasse() == Classe.cPalRes) {
            if (palReservada(token, "read")) {
                consumirToken(Classe.cPalRes);                
                consumirToken(Classe.cParEsq);
                codigo += "scanf(\"";  
                var_read();
                codigo = codigo.substring(0, codigo.length() - 1);
                gerarCodigoParametros(true);
                codigo += ")";
                consumirToken(Classe.cParDir);                                       
            } else if (palReservada(token, "write")) {
                consumirToken(Classe.cPalRes);
                consumirToken(Classe.cParEsq); 
                codigo += "printf(\"";                                
                exp_write();                               
                gerarCodigoParametros(false);                
                codigo += ")";
                consumirToken(Classe.cParDir);                                              
            } else if (palReservada(token, "writeln")) {
                consumirToken(Classe.cPalRes);
                consumirToken(Classe.cParEsq);
                codigo += "printf(\"";                 
                exp_write();
                gerarCodigoParametros(false);                
                codigo += ");\n";
                codigo += "printf(\"\\n\")";
                consumirToken(Classe.cParDir);                                
            } else if (palReservada(token, "for")){            
                comandoFor();
            }else if(palReservada(token, "repeat")){
                comandoRepeat();
            }else if(palReservada(token, "if")){                
                comandoIf();                
            }else if(palReservada(token, "while")){
                comandoWhile();
            }
        }else if(token.getClasse() == Classe.cId){                 
            verificar_identificador();
            atribuicao();                                                                 
        }else{              
            token = lexico.nextToken();
        }
    }

    private void atribuicao(){
        token = lexico.nextToken();
        if(token.getClasse() == Classe.cAtribuicao){
            codigo += "=";
            token = lexico.nextToken();
            expressao();
        }else{
            erro("Expressão inválida.");
        }
    }

    private void comandoWhile() {
        codigo += "while";
        token = lexico.nextToken();                
        if(token.getClasse() == Classe.cParEsq){  
            codigo += "(";          
            expr_logica();   
            codigo += ")";             
            if(palReservada(token, "do")){
                codigo += "{\n";
                token = lexico.nextToken();                        
                if(palReservada(token, "begin")){
                    token = lexico.nextToken();
                    sentencas();                            
                    token = lexico.nextToken();
                    codigo += "}";
                }else{
                    erro("'begin' esperado.");
                }
            }else{
                erro("'do' esperado.");
            } 
        }              
    }

    private void comandoRepeat() {
        repeat = true;
        codigo += "do{\n";
        token = lexico.nextToken();        
        sentencas();        
        if(palReservada(token, "until")){            
            token = lexico.nextToken();
            consumirToken(Classe.cParEsq);
            codigo += "}while(";
            expr_logica();
            consumirToken(Classe.cParDir);
            codigo += ")";            
        }else{
            erro("'until' esperado.");
        }
    }

    private void comandoIf() {
        codigo += "if";
        token = lexico.nextToken(); 
        consumirToken(Classe.cParEsq); 
        codigo += "(";       
        expr_logica();
        consumirToken(Classe.cParDir);
        codigo += ")";
        if(palReservada(token, "then")){
            token = lexico.nextToken();  
            codigo += "{\n";                      
            if(palReservada(token, "begin")){
                token = lexico.nextToken();                            
                sentencas();                                            
                token = lexico.nextToken();
                pFalsa(); 
                codigo += "}";                               
            }else{
                erro("'begin' esperado.");
            }
        }else{
            erro("'then' esperado.");
        }        
    }

    private void comandoFor() {                
        consumirToken(Classe.cPalRes);        
        codigo += "for(";
        String varName;
        if(token.getClasse() == Classe.cId){
            if(verificar_Variavel(token.getValor().getTexto())){
                varName = token.getValor().getTexto();
                codigo += varName;
                token = lexico.nextToken();
                consumirToken(Classe.cAtribuicao); 
                codigo += "=";                                                       
                expressao();   
                codigo += ";";  
                if(palReservada(token, "to")){
                    codigo += varName + "<=";
                    token = lexico.nextToken();                                        
                    expressao();   
                    codigo += ";";                                        
                    if(palReservada(token, "do")){                        
                        codigo += varName + "++){\n";
                        token = lexico.nextToken();
                        if(palReservada(token, "begin")){
                            token = lexico.nextToken();                                                                                                        
                            sentencas(); 
                            token = lexico.nextToken();
                            if(token.getClasse() != Classe.cPontoVirg){
                                erro("Ponto e vírgula esperado.");                                
                            }   
                            codigo += "}";                        
                        }
                    }else{
                        erro("Expressão inválida.");
                    }                              
                }else{
                    erro("Expressão inválida.");
                } 
            }else{
                erro("Variável não declarada.");
            }
        }else{
            erro("Expressão inválida.");
        }                 
    }

    private void pFalsa(){
        if(palReservada(token, "else")){
            codigo += "}else{\n";
            token = lexico.nextToken();
            if(palReservada(token, "begin")){
                token = lexico.nextToken();                                    
                sentencas();
                token = lexico.nextToken();                                                                                                                                                               
            }else{
                erro("'begin' esperado.");
            }
        }         
    }

    

    private void var_read(){
        if(token.getClasse() == Classe.cId){
            tipo_parametro();                        
            token = lexico.nextToken();
            if(token.getClasse() == Classe.cVirg){
                token = lexico.nextToken();
                var_read();
            }
        }else{
            erro("Identificador esperado.");
        }
    }

    private void exp_write(){
        if(token.getClasse() == Classe.cId || 
            token.getClasse() == Classe.cString || 
            token.getClasse() == Classe.cInt){            
            tipo_parametro();            
            token = lexico.nextToken();
            if(token.getClasse() == Classe.cVirg){                
                token = lexico.nextToken();
                exp_write();
            }                        
        }else{
            erro("Expressão inválida.");
        }
    }

    private void expressao(){        
        termo();
        mais_expressao();        
    }

    private void termo(){
        fator();
        mais_termo();
    }

    private void tipo_parametro(){        
        if(token.getClasse() == Classe.cId){  
            if(verificar_Variavel(token.getValor().getTexto())) 
                parametros.add(token.getValor().getTexto());
            else
                erro("Variável não declarada.");
            codigo += "%d ";
        }else if(token.getClasse() == Classe.cString){             
            codigo += token.getValor().getTexto();
        }else if(token.getClasse() == Classe.cInt){            
            codigo += String.valueOf(token.getValor().getInteiro());
        }
    }

    private void fator() {                                       
        if(token.getClasse() == Classe.cId || token.getClasse() == Classe.cInt){            
            if(token.getClasse() == Classe.cId){             
                verificar_identificador();                
            }else{
                codigo += token.getValor().getInteiro();
            } 
            token = lexico.nextToken();                                  
        }else if(token.getClasse() == Classe.cParEsq){
            codigo += "(";
            token = lexico.nextToken(); 
            expressao();
            consumirToken(Classe.cParDir);
            codigo += ")";
        }else{
            erro("Expressão inválida.");
        }
    }

    private void mais_expressao(){
        if(token.getClasse() == Classe.cAdicao || token.getClasse() == Classe.cSubtracao){            
            codigo += token.getValor().getTexto();
            token = lexico.nextToken();            
            termo();
            mais_expressao();
        }
    }

    private void mais_termo(){
        if(token.getClasse() == Classe.cMultiplicacao || token.getClasse() == Classe.cDivisao){            
            codigo += token.getValor().getTexto();
            token = lexico.nextToken();            
            fator();
            mais_termo();
        }
    }    

    private void termo_logico() {
        fator_logico();
        mais_termo_logico();        
    }
    
    private void expr_logica(){
        termo_logico();
        mais_expr_logica();
    }

    private void fator_logico() {
        if(token.getClasse() == Classe.cParEsq){
            codigo += "(";
            token = lexico.nextToken();            
            expr_logica();            
            consumirToken(Classe.cParDir);
            codigo += ")";
        }else if(palReservada(token, "not")){
            codigo += "!";
            token = lexico.nextToken();
            expr_logica();
        }else if(palReservada(token, "true") ||
            palReservada(token, "false")){
            codigo += token.getValor().getTexto();
            token = lexico.nextToken();
        }else{
            relacional();            
        }
    }

    private void relacional() {
        expressao();
        if(token.getClasse() == Classe.cIgual ||
            token.getClasse() == Classe.cMaior ||
            token.getClasse() == Classe.cMenor ||
            token.getClasse() == Classe.cMaiorIgual || 
            token.getClasse() == Classe.cMenorIgual ||
            token.getClasse() == Classe.cDiferente){
            gerarCodigoOperadorLogico();
            token = lexico.nextToken();
            expressao();                     
        }
    }

    private void mais_expr_logica() {
        if(palReservada(token, "or")){
            codigo += " || ";
            token = lexico.nextToken();
            termo_logico();
            mais_expr_logica();
        }
    }

    private void mais_termo_logico() {
        if(palReservada(token, "and")){
            codigo += " && ";
            token = lexico.nextToken();
            fator_logico();
            mais_termo_logico();
        }
    }

    //Tradução do código
    private void gerarCodigoOperadorLogico(){
        if(repeat){
            //inverte operador lógico
            repeat = false;
            if(token.getClasse() == Classe.cIgual)
                codigo += "!=";
            else if(token.getClasse() == Classe.cDiferente)
                codigo += "==";
            else if(token.getClasse() == Classe.cMaior)
                codigo += "<=";
            else if(token.getClasse() == Classe.cMenor)
                codigo += ">=";
            else if(token.getClasse() == Classe.cMaiorIgual)
                codigo += "<";
            else if(token.getClasse() == Classe.cMenorIgual)
                codigo += ">";            
        }else{
            if(token.getClasse() == Classe.cIgual)
                codigo += "==";
            else if(token.getClasse() == Classe.cDiferente)
                codigo += "!=";
            else
                codigo += token.getValor().getTexto();
        }
    }

    private void gerarCodigoParametros(boolean leitura){               
        codigo += (parametros.isEmpty()) ? "\"" : "\", ";
        for(int i = 0; i < parametros.size(); i++){
            if(i < parametros.size() - 1){
                codigo += (leitura) ? "&"+parametros.get(i) + ", " : parametros.get(i) + ", ";
            }else{
                codigo += (leitura) ? "&"+parametros.get(i): parametros.get(i);
            }
        }            
        parametros.clear();
    }

    private void gerarCodigoVariaveis(){
        codigo += "int ";
        for(int i = 0; i < variaveis.size(); i++){
            if(i < variaveis.size() - 1){
                codigo += variaveis.get(i).getVarName() + ", ";
            }else{
                codigo += variaveis.get(i).getVarName() + ";\n";
            }
        }
    }

    private void gerarCodigo() {
        try {
            fw.write(codigo);
        } catch (IOException e) {
            System.out.println("Erro ao escrever no arquivo de saida");
            e.printStackTrace();
        }
    }
    // Método para verificar se um identificador já foi declarado
    private void verificar_identificador(){
        boolean varDeclarada = false;              
        for (simbolo simbolo : variaveis) {
            if(simbolo.getVarName().equals(token.getValor().getTexto()))
                varDeclarada = true;
        }                
        if(!varDeclarada){
            erro("Variável não declarada.");
        }
        codigo += token.getValor().getTexto();
    }
    // Método para verificar se uma variável já foi declarada
    private boolean verificar_Variavel(String var){        
        for(int i = 0; i < variaveis.size(); i++){
            if(variaveis.get(i).getVarName().equals(var)){
                return true;
            }
        }
        return false;
    }

    private void consumirToken(Classe classeEsperada) {
        if (token.getClasse() == classeEsperada) {
            token = lexico.nextToken();
        } else {
            erro(classeEsperada + " esperado.");            
        }
    }
     // Método para verificar se um token é uma palavra reservada específica
    private boolean palReservada(Token token, String palavra) {
         return token.getClasse() == Classe.cPalRes && token.getValor().getTexto().equals(palavra);
    }

    private void erro(String mensagem) {
        System.out.println(token.getLinha() +","+ token.getColuna() + ": " + mensagem);
        System.exit(1);
    }
}