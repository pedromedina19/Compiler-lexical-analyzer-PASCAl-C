package sintatico;

// Classe que representa um símbolo em um contexto sintático
class simbolo{
    // Enumeração para os tipos de símbolos possíveis
    public enum Tipo{
        INTEIRO, FLOAT, STRING;
    }

    private String var;
    private Tipo tipo;
    private String valor;

    simbolo(String var, Tipo tipo, String valor){
        this.var = var;
        this.tipo = tipo;
        this.valor = valor;
    }
    // Método para obter o nome da variável
    public String getVarName(){
        return this.var;
    }
}