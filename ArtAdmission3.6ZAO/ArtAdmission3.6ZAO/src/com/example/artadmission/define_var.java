package com.example.artadmission;

public final class define_var {
	public static int maxStudentPerGroup = 10;   //ÿ�������ϵ����
	public static int tickN = 0;
//	public static int[] needScore = new int[maxStudentPerGroup];
//	public static boolean[] absentStudent = new boolean[maxStudentPerGroup];
	public static float[] score = new float[maxStudentPerGroup];
	public static int checked[] = new int[define_var.maxStudentPerGroup];	
	public static String subject="",room="",group="",userNameStr="",examTypeStr="";
	public static String std;
	/***���Ե�IP/�˿ں�****/
	public static String RHOST = null;
	public static String TPORT = null;
	/***״̬**/
	public static int loginLossProcess = 0;	//��¼ʧ����
	public static int loginSuccessProcess = 1;	//��¼�ɹ���
	public static int agreeProcess = 2;			//�����
	public static int groupStartProcess = 3;	//���鿪ʼ������
	public static int studentStartProcess = 4;	//ĳ������ʼ������
	public static int checkStopProcess = 5;		//��¼������
	public static int uploadScoreProcess = 6;	//�����ύ�ɹ���
	public static int groupStopProcess = 7;		//���鿼�Խ�����
	/****handle*****/
	public static int conOKHandle = 3;		//���ӳɹ�
	public static int conNOHandle = 4;		//����ʧ��
	public static int logOKHandle = 5;		//��¼�ɹ�
	public static int logNOHandle = 6;		//��¼ʧ��
	public static int newtHandle = 7;		//����
	public static int loseHandle = 8;		//ȱ��
	public static int uploadScoreHandle = 9;		//�յ�����
	public static int uploadScoreHandle_NO = 10;		//�յ�����,ʧ��
	public static int infoHandle = 11;		//�ѿ��ʹ�������	
	
	/***ָ��*****/
	public static String connect = "CON";			//����
	public static String connect_ok = "CON_OK|";		//���ӳɹ�
	public static String connect_no = "CON_NO|";		//����ʧ��
	public static String log = "LOG";			//��¼
	public static String log_ok = "LOG_OK|";		//��¼�ɹ�
	public static String log_no = "LOG_NO|";		//��¼ʧ��
	public static String newt = "NEWT|";			//���͸��ҵ���Ϣ
	public static String newt_ack = "NEWT_OK";	//���͵Ļظ�
	public static String lose = "LOSE|";			//ȱ��
	public static String lose_ack = "LOSE_OK";	//ȱ���Ļظ�
	public static String uploadScore = "SCORE";			//�ϴ�����
	public static String uploadScore_ack = "SCORE_OK|";	//�ϴ������Ļظ�
	public static String uploadScore_NO = "SCORE_NO|";	//�ϴ������Ļظ���ʧ��
	public static String info = "INFO|";			//�㲥��Ϣ�������ѿ��ʹ�������
	public static String info_ack = "INFO_OK";	//�ѿ��ʹ��������Ļظ�
	
	public static int newt_endHandle = 11;		//�����õ�-----�ѿ��ʹ�������	
	public static String newt_end = "NEWT_END|";			//���͸��ҵ���Ϣ
	public static String newt_end_ack = "NEWT_END_OK";	//���͵Ļظ�
}
