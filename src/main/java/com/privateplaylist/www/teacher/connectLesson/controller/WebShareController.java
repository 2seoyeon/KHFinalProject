package com.privateplaylist.www.teacher.connectLesson.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.mail.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.privateplaylist.www.dto.Webshare;
import com.privateplaylist.www.member.vo.Member;
import com.privateplaylist.www.teacher.connectLesson.service.WebShareService;

import common.util.Paging;

@Controller
@RequestMapping("/teacher")
public class WebShareController {

	
	@Autowired
	WebShareService webShareService;
	
	
	//연결된 과외 목록
	@RequestMapping("/connectedlesson")
	public String lessonList(Model model, HttpSession session) {
		
//		System.out.println("lessonList 접속 테스트");
		
//		int teaNo = 9;
		
		Member sessionMember = (Member) session.getAttribute("loginUser");
		
		int teaNo = sessionMember.getUserNo();
		
//		System.out.println(teaNo);
		
		
		//연결된 과외 조회
		List<Map<String, Object>> connectedLessonList = webShareService.selectConnectedLesson(teaNo);
		
//		System.out.println(connectedLessonList);
		
		model.addAttribute("connectedLessonList", connectedLessonList);
		
		return "/teacher/connectedLesson/lessonList";
	} 
	
	
	//자료실 목록
	@RequestMapping("/webshare")
	public String webshareList(Model model, int no, HttpServletRequest req) {
		
//		System.out.println("자료실 왔나?");
		
//		System.out.println(no); //no : conn_lesson_no
		
		
		//요청 파라미터를 전달하여 paging 객체 생성하기
		Paging paging = webShareService.webShareListPaging(req, no);
		
		//연결된 과외 번호의 자료실 조회
		List<Map<String, Object>> webShareList = webShareService.selectWebShareList(no, paging);
		
//		System.out.println(webShareList);
		
		
		//자료실 리스트
		model.addAttribute("webShareList", webShareList);
		
		//페이징 결과 전달
		model.addAttribute("paging", paging);
		
		//conn_lesson_no
		model.addAttribute("cno", no);
		
		
		return "/teacher/webShare/webShareList";
	}
	
	
	//자료실 목록 > 체크박스 선택 > 삭제
	@RequestMapping("/webshare/delete")
	public String webshareDelete(Model model, HttpServletRequest req, int no ) {
		
//		System.out.println("delete 왔나?");
		
//		System.out.println(no); //no : conn_lesson_no
		
		
		//root context
		String root = req.getContextPath();
		
		String[] checkRowArr =  req.getParameterValues("checkRow");
		
		if(checkRowArr == null) {
			
			model.addAttribute("alertMsg", "선택하신 글이 없습니다.");
			model.addAttribute("url", root+"/teacher/webshare?no="+no);

			return "/admin/notice/error";
			
		} else {
			
			//체크박스 선택시 삭제 실행
			for (int i = 0; i < checkRowArr.length; i++) {
				
				int shareNo = Integer.parseInt(checkRowArr[i]);
				
				int res = webShareService.deleteWebShare(shareNo);
			}
			
			//삭제 완료
			return "redirect:/teacher/webshare?no="+no;
		}
		
	}
	
	
	
	//자료실 목록 > 글 작성
	@RequestMapping("/webshare/write")
	public String webshareWrite(Model model, int no) {
		
//		System.out.println("글 작성 왔나?");
		
//		System.out.println(no);
		
		model.addAttribute("no", no); //no : conn_lesson_no
		
		return "/teacher/webShare/webShareWrite";
	}
	
	
	
	//자료실 목록 > 글 작성  > 등록
	@RequestMapping("/webshare/insert")
	public String webshareInsert(@RequestParam List<MultipartFile> files, HttpSession session, Webshare webShare, int connLessonNo) {
		
//		System.out.println("글 등록 왔나?");
		
//		System.out.println(connLessonNo);
		
		String root = session.getServletContext().getRealPath("/");
		
		webShare.setConnLessonNo(connLessonNo);
		
		int res = webShareService.insertWebShare(files, root, webShare);
		
		return "redirect:/teacher/webshare?no="+connLessonNo; 
	}
	
	
	
	//자료실 목록 > 글 보기
	@RequestMapping("/webshare/detail")
	public String webshareDetail(Model model, int no) {
		
//		System.out.println("글상세 왔나?");
		
//		System.out.println("shareno : "+no);
		
		Map<String, Object> webShareDetail = webShareService.selectWebShareDetail(no);
		
		model.addAttribute("webShareDetail", webShareDetail);
		
		return "/teacher/webShare/webShareDetail";
		
	}
		
	
	//자료실 파일 다운로드
	@RequestMapping("/webshare/download")
	@ResponseBody															//파일경로 지정을 위한 session				//서버에 저장된 파일 이름
	public FileSystemResource noticeDownload(HttpServletResponse response, HttpSession session, String ofname, String rfname) {
											//response header 지정을 위한 response					//사용자가 올린 파일 이름
		
//		System.out.println("다운로드 왔나?");
		
		String readFolder = session.getServletContext().getRealPath("/resources/upload");
		
		//FileSystemResource
		FileSystemResource downFile = new FileSystemResource(readFolder+"/"+rfname);
		
		
		try {
			
			response.setHeader("Content-Disposition", "attachment; filename="+URLEncoder.encode(ofname, "UTF-8"));
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return downFile;
			
	}
	
	
	//자료실 글 상세 > 글 삭제
	@RequestMapping("/webshare/deleteone")
	public String webshareDeleteOne(int sno, int cno) {
		
//		System.out.println("글삭제 왔나?");
		
//		System.out.println("sno:"+sno); //share_no
//		System.out.println("cno:"+cno); // conn_lesson_no
		
		int res = webShareService.deleteWebShare(sno);
		
		return "redirect:/teacher/webshare?no="+cno; //여기서 필요한건 conn_lesson_no
	}
	
	
	
}
