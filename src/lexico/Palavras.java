package lexico;

// Classe que representa uma coleção de palavras reservadas
public class Palavras {
    // Array de palavras reservadas
    private static String[] words = {"and", "array", "begin", "case", "const", "div", "do", "downto", "else", "end",
    "file", "for", "function", "goto", "if", "in", "label", "mod", "nil", "not", "of", "or","packed", "procedure", "program", 
    "record", "repeat", "set","then", "to","type", "until", "var", "while", "with", "integer", "read", "write", "writeln"};
    // Método para comparar uma string dada com as palavras reservadas
    public static boolean compare(String str){
        // Itera sobre o array de palavras reservadas
        for (String string : words) {
            // Verifica se a string dada (sem diferenciar maiúsculas e minúsculas) corresponde a alguma palavra reservada
            if(str.toLowerCase().equals(string)){
                return true; // Retorna true se houver correspondência
            }
        }
        return false; // Retorna false se não houver correspondência
    }
}