/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject2;

import java.io.Serializable;

/**
 *
 * @author ggunes
 */
public class Dice implements Serializable {

    private int number;
    private boolean held;

    public void rollDice() {
        if (held == false) {
            number = (int) (Math.random() * 6) + 1;
        }
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

}
