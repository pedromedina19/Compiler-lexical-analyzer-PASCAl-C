package lexico;

// Enumeração que representa as classes dos tokens no analisador léxico
public enum Classe {
    cId,            // Identificador
    cInt,           // Número inteiro
    cParEsq,        // Parêntese esquerdo "("
    cParDir,        // Parêntese direito ")"
    cString,        // String
    cVirg,          // Vírgula ","
    cPontoVirg,     // Ponto e vírgula ";"
    cAdicao,        // Operador de adição "+"
    cSubtracao,     // Operador de subtração "-"
    cMultiplicacao, // Operador de multiplicação "*"
    cDivisao,       // Operador de divisão "/"
    cAtribuicao,    // Operador de atribuição ":="
    cMenor,         // Operador de menor "<"
    cMaior,         // Operador de maior ">"
    cMenorIgual,    // Operador de menor ou igual "<="
    cMaiorIgual,    // Operador de maior ou igual ">="
    cIgual,         // Operador de igualdade "="
    cDiferente,     // Operador de diferença "<>"
    cEOF,           // Fim de arquivo
    cDoisPontos,    // Dois pontos ":"
    cPalRes,        // Palavra reservada
    cPonto          // Ponto "."
}