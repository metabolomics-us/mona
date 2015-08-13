package moa.splash

import moa.Spectrum

/**
 * defines the splash, as outlined here
 *
 * splash.fiehnlab.ucdavis.edu
 *
 */
class Splash {


    static belongsTo = [spectrum:Spectrum]

    static constraints = {
    }

    static mapping = {
        block1 nullable:true
        block2 nullable:true
        block3 nullable:true
        block4 nullable:true

    }
    String splash

    String block1

    String block2

    String block3

    String block4

    Spectrum spectrum

    def beforeValidate() {

        if(splash!= null){
            String[] blocks = splash.split("-");

            if(blocks.size() == 4){
                block1 = blocks[0]
                block2 = blocks[1]
                block3 = blocks[2]
                block4 = blocks[3]
            }
            else{
                throw new RuntimeException("invalid number of blocks!")
            }
        }
    }
}