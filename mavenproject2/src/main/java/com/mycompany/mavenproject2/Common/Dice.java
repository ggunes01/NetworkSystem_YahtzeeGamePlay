/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject2.Common;

import java.io.Serializable;

/**
 *
 * @author ggunes
 */
//This class is created to manage each dice separately
public class Dice implements Serializable {

    private int number; //Dice Value
    private boolean held; //Statu of Dice (is it held)

    public int rollDice() {
        //Dice rolling function, 
        if (held == false) {
            number = (int) (Math.random() * 6) + 1;
        }
        return number ;
    }

    public void changeStatu() {
        this.held = !this.held;
    }

    public boolean isHeld() {
        return held;
    }

    public int getNumber() {
        return number;
    }

    public void setHeld(boolean held) {
        this.held = held;
    }

    public void setNumber(int number) {
        this.number = number;
    }

}

