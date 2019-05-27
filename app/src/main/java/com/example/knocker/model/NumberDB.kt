package com.example.knocker.model

/**
 * TO DELETE ???
 * @author Florian Striebel
 */
object NumberAndMailDB {
   public fun convertSpinnerStringToChar(type :String ):Char{ //récupère la valeur du spinner pour en sortir un caractère

       if(type.equals("Mobile")){
            return 'M'
        }else if (type.equals("Bureau")){
            return 'B'
        }else if(type.equals("Domicile")){
            return 'D'
        }else if(type.equals("Principal")){
            return 'P'
        }else if(type.equals("Autre")){
            return 'A'
        }else if(type.equals("Personnalisé")){
            return 'R'
        }else if(type.equals("Ecole")){
            return 'E'
        }
       return ' '
    }
    public fun convertSpinnerMailStringToChar(type :String,mail:String):String {//méthode pour ajouter le champ ou non mail
        if(mail.equals("")){
            return ""
        }else{
            return convertSpinnerStringToChar(type).toString()
        }
    }
    public fun extractStringFromNumber(numberDB : String):String {//récupère le caractère qui se trouve a la fin du num de tel pour retrouver la chaine de caractère correspondant au spinner
        if(numberDB.length>2) {
            val char: Char = numberDB.get(numberDB.length - 1)
            println("test char " + char)
            if (char.equals('M')) {
                return "Mobile"
            } else if (char.equals('B')) {
                return "Bureau"
            } else if (char.equals('D')) {
                return "Domicile"
            } else if (char.equals('P')) {
                return "Principal"
            } else if (char.equals('A')) {
                return "Autre"
            } else if (char.equals('R')) {
                return "Personnalisé"
            } else if (char.equals('E')) {
                return "Ecole"
            }
        }
        return "X"
    }
    public fun numDBAndMailDBtoDisplay(numberDBOrMailDB: String):String{
        if(numberDBOrMailDB.length>2) {
            return numberDBOrMailDB.substring(0, numberDBOrMailDB.length - 1)
        }else{
            return ""
        }
    }
}