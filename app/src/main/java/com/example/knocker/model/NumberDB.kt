package com.example.knocker.model

/**
 * TO DELETE ???
 * @author Florian Striebel
 */
object NumberAndMailDB {
   public fun convertSpinnerStringToChar(type :String ):String{ //récupère la valeur du spinner pour en sortir un caractère

       if(type.equals("Mobile")){
            return "mobil"
        }else if (type.equals("Bureau")){
            return "work"
        }else if(type.equals("Domicile")){
            return "home"
        }else if(type.equals("Principal")){
            return "principal"
        }else if(type.equals("Autre")){
            return "other"
        }else if(type.equals("Personnalisé")){
            return "custom"
        }else if(type.equals("Ecole")){
            return "school"
        }
       return " "
    }
    public fun convertSpinnerMailStringToChar(type :String,mail:String):String {//méthode pour ajouter le champ ou non mail
        if(mail.equals("")){
            return ""
        }else{
            return convertSpinnerStringToChar(type).toString()
        }
    }
}