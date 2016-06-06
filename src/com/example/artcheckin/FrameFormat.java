package com.example.artcheckin;

public class FrameFormat {
	public static int maxStudentPerGroup = 10;   //ÿ�������ϵ����
	public static byte frameHead = 0x18;		 //֡ͷ		
	public static byte frameEnd = 0x0A;			 //֡β
	public static int usernameLength = 6;		 //�û�������
	public static int passwordLength = 6;		 //���볤��
	public static byte[] userName= new byte[usernameLength];//�û���
	public static int IO_BUFFER_SIZE = 2*1024;
	
	public static String LOGINWORKERSTYLE_CHECKIN = "0";  //��¼�ߵ�����
	public static int MAXRECONNTIMES = 5;  //�����������
	public static int MAXRETRANSTIMES = 5;  //����ط�����
	public static int STATUS_NOTEXAMED = 0;	//��û����
	public static int STATUS_EXAMING = 1;	//���ڿ���
	public static int STATUS_HAVAEXAMED = 2;	//�Ѿ�����
	public static int STATUS_ABSENT = 3;	//ȱ��
	public static int STATUS_NOPERSON = 4;  //����
	public static int STATUS_NOTEXAMEDOREXAMING = 5;  //����
//	public static String FLAG_ISFIRSTSTART = "1";   //��Ϊ�����һ�������ģ�����Ϊ1
//	public static String FLAG_ISNOTFIRSTSTART = "0";   //��Ϊ�����һ�������ģ�����Ϊ1
	
	public static String FLAG_ISWAITINGBEGACK = "2";
	public static String FLAG_ISWAITINGENDACK = "3";
	public static String FLAG_ISWAITINGLOSEACK = "4";
	/***״̬**/
	public static int PROCESS_DIALOGBACKDOWN = 8;	//processdialog�ķ��ؼ�������
	public static int PROCESS_CONNECTSUCCESS = 1;	//��¼ʧ����
	public static int PROCESS_LOGINSUCCESS = 2;	//��¼ʧ����
	public static int PROCESS_SERVERSOCKETCONSUCCESS = 3;	//��¼ʧ����
	public static int PROCESS_SERVERSOCKETCONTIMEOUT = 4;	//��¼ʧ����
	public static int PROCESS_HANDLE_SERVERDISCONNECT = 5;	//��¼ʧ����
	public static int PROCESS_CONNECTFAIL = 6;	//��¼ʧ����
	public static int PROCESS_LOGINFAIL = 7;	//��¼ʧ����
	
	public static int STARTOK = 1;
	public static int STARTNO = 2;
	public static int STARTNOACK = 3;
	/***
	 * ���У���¼��--->������
	 *1	    1	    1	    30	     2	    1
	 *֡ͷ 	֡����	֡��  	������	У��λ	֡β
	 ***/
	public static int totalSendLength = 36;		//����֡���ܳ���
	//��������
	public static String COMMAND_CONNECT = "CON";			//����֡	
	public static String COMMAND_LOGIN = "LOG";			//��¼֡
	public static String COMMAND_BEGINEXAM ="BEG";				//��ʼ��¼֡
	public static String COMMAND_STOPEXAM = "END";			//������¼֡
	public static String COMMAND_ABSENT = "LOSE";	    //������¼
	//��������
	public static String COMMAND_UPDATEINFO = "NEWW";
	public static String COMMAND_UPDATEINFOOK = "NEWW_OK";
//	public static String COMMAND_UPDATEINFOACK = "NEWW_ACK";
	public static int COMMANDLEN_UPDATEINFO =11 ;	    //COMMAND_UPDATEINFO����
	
	public static String COMMAND_INFO = "INFO";	    //ϵͳ������Ϣ
	public static String COMMAND_INFOOK = "INFO_OK";	    //ϵͳ������Ϣ
	public static int COMMANDLEN_INFO =1 ;	    //COMMAND_UPDATEINFO����
	//�������ͣ����ܣ��ܾ����ظ�
	public static String COMMAND_CONNECTOK = "CON_OK";			//����֡	
	public static String COMMAND_CONNECTNO = "CON_NO";			//����֡	
	public static String COMMAND_LOGINOK = "LOG_OK";			//��¼֡
	public static String COMMAND_LOGINNO = "LOG_NO";			//��¼֡
	public static String COMMAND_BEGINEXAMOK ="BEG_OK";				//��ʼ��¼֡
	public static String COMMAND_BEGINEXAMNO ="BEG_NO";				//��ʼ��¼֡
	public static String COMMAND_ABSENTOK = "LOSE_OK";	    //������¼
	public static String COMMAND_ABSENTNO = "LOSE_NO";	    //������¼
	public static String COMMAND_STOPEXAMOK = "END_OK";			//������¼֡
	public static String COMMAND_STOPEXAMNO = "END_NO";			//������¼֡
//	public static String COMMAND_ABSENTACK = "LOSE_ACK";	    //������¼
	
	
	//�������⣬��������������ĸ���
	public static int COMMANDLEN_CONNECTOK = 1;			//����֡	
	public static int COMMANDLEN_CONNECTNO = 1;			//����֡	
	public static int COMMANDLEN_LOGINOK = 6;			//��¼֡
	public static int COMMANDLEN_LOGINNO = 1;			//��¼֡
	public static int COMMANDLEN_BEGINEXAMOK =4;				//��ʼ��¼֡
	public static int COMMANDLEN_BEGINEXAMNO =1;				//��ʼ��¼֡
	public static int COMMANDLEN_STOPEXAMOK = 4;			//������¼֡
	public static int COMMANDLEN_STOPEXAMNO = 1;			//������¼֡
	public static int COMMANDLEN_ABSENTOK = 4;	    //������¼
	public static int COMMANDLEN_ABSENTNO = 1;	    //������¼
	
	
	//���ļ�������
	public static String ROOTFILENAME = "ArtCheckIn";
	public static String pianoStr = "piano";				//����
	public static String danceStr = "dance";				//�赸
	public static String vocalStr = "vocal";				//����
	public static String othersStr = "others";				//����
	//���Խ׶�
	public static String firstStr = "First";				//����
	public static String twiceStr = "Second";				//����
	public static String threeStr = "Third";				//����
	/****handle*****/
	//connectThread-->LoginActivity
	public static int HANDLE_SERVERSOCKETCONTIMEOUT = 2;   //CON5����û���յ���������Ӧ
	public static int HANDLE_SERVERDISCONNECT = 16;   //CON5����û���յ���������Ӧ
	public static int HANDLE_STARTEXAMACKTIMEOUT = 17;   //CON5����û���յ���������Ӧ
	public static int HANDLE_STOPEXAMACKTIMEOUT = 18;   //CON5����û���յ���������Ӧ
	public static int HANDLE_ABSENTTIMEOUT = 19;   //CON5����û���յ���������Ӧ
	public static int HANDLE_SERVERSOCKETCONSUCCESS = 20;   //socket���ӳɹ�
	//receiveThread-->LoginActivity
	public static int HANDLE_CONNECTSUCCESS = 3;		//��¼�ɹ�
	public static int HANDLE_CONNECTFAIL = 4;			//��¼ʧ��
	public static int HANDLE_LOGINSUCCESS = 5;		//��¼�ɹ�
	public static int HANDLE_LOGINFAIL = 6;			//��¼ʧ��
	//receiveThread-->MainActivity
	public static int HANDLE_STRATEXAMOK = 7;			//��¼ʧ��
	public static int HANDLE_STRATEXAMNO = 8;			//��¼ʧ��
	public static int HANDLE_STOPEXAMOK = 13;			//��¼ʧ��
	public static int HANDLE_STOPEXAMNO = 14;			//��¼ʧ��
	public static int HANDLE_ABSENTOK = 10;			//���´�������Ϣ
	public static int HANDLE_ABSENTNO = 9;			//���´�������Ϣ
	public static int HANDLE_UPDATEINFO = 11;			//���´�������Ϣ
	public static int HANDLE_INFO = 12;			//���´�������Ϣ
}
