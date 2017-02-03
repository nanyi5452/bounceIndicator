package com.nanyi545.www.bounceindicatorlib;

/**
 * Created by Administrator on 2016/10/11.
 */
public class MathVector2D {

    public static class VectorF{

        @Override
        public String toString() {
            return "("+dx+","+dy+")";
        }

        public float dx;
        public float dy;
        public VectorF(float dx, float dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public float getLength(){
            return (float) Math.sqrt(dx*dx+dy*dy);
        }

        public float dotProduct(Vector other){
            return this.dx*other.dx+this.dy*other.dy;
        }


        public double getAngle(){
            Vector v1=new Vector(100,0);
            double cos=  (float) this.dotProduct(v1) / this.getLength()/v1.getLength();
            if (dy>=0)
                return (Math.acos(cos)/ Math.PI*180);
            else return (360- Math.acos(cos)/ Math.PI*180);
        }


        public void addAngle(float offsetAngle){
            double newAngle=getAngle()+offsetAngle;
            double length=getLength();
            dy = (int) (length * Math.sin(newAngle/180* Math.PI));
            dx = (int) (length * Math.cos(newAngle/180* Math.PI));
        }

        public void scaleTo(float length){
            float ratio=length/getLength();
            this.dx=(dx*ratio);
            this.dy=(dy*ratio);
        }

    }



    public static class Vector{

        @Override
        public String toString() {
            return "("+dx+","+dy+")";
        }

        public int dx;
        public int dy;
        public Vector(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public double getLength(){
            return Math.sqrt(dx*dx+dy*dy);
        }

        public int dotProduct(Vector other){
            return this.dx*other.dx+this.dy*other.dy;
        }


        public double getAngle(){
            Vector v1=new Vector(100,0);
            double cos=  (float) this.dotProduct(v1) / this.getLength()/v1.getLength();
//            System.out.println(dx+"--"+dy+"cos:"+cos+ "    dotProduct:"+((float) this.dotProduct(v1)));
            if (dy>=0)
            return (Math.acos(cos)/ Math.PI*180);
            else return (360- Math.acos(cos)/ Math.PI*180);
        }


        public void addAngle(int offsetAngle){
            double newAngle=getAngle()+offsetAngle;
            double length=getLength();
            dy = (int) (length * Math.sin(newAngle/180* Math.PI));
            dx = (int) (length * Math.cos(newAngle/180* Math.PI));
        }

        public void scaleTo(int length){
            double ratio=length/getLength();
            this.dx=(int)(dx*ratio);
            this.dy=(int)(dy*ratio);
        }

    }




}
