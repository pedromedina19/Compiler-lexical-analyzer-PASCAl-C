package lexico;

import java.io.BufferedReader;
import java.io.IOException;

// Classe que implementa o analisador léxico
public class Lexico {

    private BufferedReader br;    // Leitor de buffer para ler caracteres do código-fonte
    private char caractere;        // Caractere atual sendo analisado
    private int linha;              // Número da linha atual no código-fonte
    private int coluna;             // Número da coluna atual no código-fonte
    private boolean ignorar = false; // Flag para ignorar caracteres em certos casos
    
    // Construtor que recebe um BufferedReader para ler o código-fonte
    public Lexico(BufferedReader br) {
        this.br = br;
        linha = 1;
        coluna = 0;
        caractere = nextChar();  // Inicializa o primeiro caractere             
    }

    // Método para obter o próximo token do código-fonte
    public Token nextToken() {
        StringBuilder lexema = new StringBuilder();
        while (caractere != 65535) { // 65535 representa o fim do fluxo
            if (Character.isLetter(caractere)) {
                return processIdentifier(lexema);
            } else if (Character.isDigit(caractere)) {
                return processNumber(lexema);
            } else if (Character.isWhitespace(caractere)) {
                processWhitespace();
            } else {
                Token token = processSymbol(lexema);
                if (token != null) {
                    return token;
                }
            }            
        }
        return new Token(Classe.cEOF, linha, coluna - 1);
    }
    // Método para processar identificadores
    private Token processIdentifier(StringBuilder lexema) {
        Token token = new Token(Classe.cId, linha, coluna);
        while (Character.isLetter(caractere) || Character.isDigit(caractere)) {
            lexema.append(caractere);
            caractere = nextChar();
        }
        // Verifica se o identificador é uma palavra reservada
        if (Palavras.compare(lexema.toString())) {
            token.setClasse(Classe.cPalRes);
        }
        token.setValor(new Valor(lexema.toString()));
        return token;
    }
    // Método para processar números
    private Token processNumber(StringBuilder lexema) {
        Token token = new Token(Classe.cInt, linha, coluna);
        while (Character.isDigit(caractere)) {
            lexema.append(caractere);
            caractere = nextChar();
        }
        token.setValor(new Valor(Integer.parseInt(lexema.toString())));
        return token;
    }
    // Método para processar espaços em branco e novas linhas
    private void processWhitespace() {
        if (caractere == '\n') {
            linha++;
            coluna = 0;
        }
        caractere = nextChar();
    }
    // Método para processar símbolos (operadores, parênteses, etc.)
    private Token processSymbol(StringBuilder lexema) {
        Token token = null;
        char aux;
        
        if (caractere == '(') {
            token = new Token(Classe.cParEsq, linha, coluna);
        } else if (caractere == ')') {
            token = new Token(Classe.cParDir, linha, coluna);
        } else if (caractere == ',') {
            token = new Token(Classe.cVirg, linha, coluna);
        } else if (caractere == ';') {
            token = new Token(Classe.cPontoVirg, linha, coluna);
        } else if (caractere == '.') {
            token = new Token(Classe.cPonto, linha, coluna);
        } else if (caractere == '+') {
            token = new Token(Classe.cAdicao, linha, coluna);
        } else if (caractere == '-') {
            token = new Token(Classe.cSubtracao, linha, coluna);
        } else if (caractere == '*') {
            token = new Token(Classe.cMultiplicacao, linha, coluna);
        } else if (caractere == '/') {
            token = new Token(Classe.cDivisao, linha, coluna);
        } else if (caractere == ':') {
            token = new Token(Classe.cDoisPontos, linha, coluna);
            aux = nextChar();
            if (aux == '=') {
                token.setClasse(Classe.cAtribuicao);
                lexema.append(caractere);
                caractere = aux;
            }
        } else if (caractere == '<') {
            token = new Token(Classe.cMenor, linha, coluna);
            aux = nextChar();
            if (aux == '=') {
                token.setClasse(Classe.cMenorIgual);
                lexema.append(caractere);
                caractere = aux;
            } else if (aux == '>') {
                token.setClasse(Classe.cDiferente);
                lexema.append(caractere);
                caractere = aux;
            }
        } else if (caractere == '>') {
            token = new Token(Classe.cMaior, linha, coluna);
            aux = nextChar();
            if (aux == '=') {
                token.setClasse(Classe.cMaiorIgual);
                lexema.append(caractere);
                caractere = aux;
            }
        } else if (caractere == '=') {
            token = new Token(Classe.cIgual, linha, coluna);
        } else if (caractere == '\'') {
            token = processString(lexema);
        } else {
            System.out.println("Outra coisa: " + caractere);
            caractere = nextChar();
        }
        if (token != null) {        
            setValor(token, lexema);
        }
        return token;
    }
    // Método para processar strings
    private Token processString(StringBuilder lexema) {
        Token token = new Token(Classe.cString, linha, coluna);
        caractere = nextChar();        
        setValor(token, lexema);    
        while (caractere != '\'' && caractere != 65535) {                        
            setValor(token, lexema);            
        }  
        if(caractere == '\'')       
            ignorar = true;
        return token;
    }
    // Método para obter o próximo caractere do código-fonte
    private char nextChar() {
        try {
            coluna++;
            return (char) br.read();
        } catch (IOException e) {
            return ' ';
        }
    }   
    // Método para definir o valor do token e avançar para o próximo caractere
    private void setValor(Token token, StringBuilder lexema){
        if(ignorar)
            ignorar = false;
        else
            lexema.append(caractere);
        token.setValor(new Valor(lexema.toString()));
        caractere = nextChar();
    }

}