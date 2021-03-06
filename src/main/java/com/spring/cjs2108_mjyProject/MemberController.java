package com.spring.cjs2108_mjyProject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.cjs2108_mjyProject.service.MemberService;
import com.spring.cjs2108_mjyProject.service.ProductService;
import com.spring.cjs2108_mjyProject.vo.BaesongVO;
import com.spring.cjs2108_mjyProject.vo.MemberVO;

@Controller
@RequestMapping("/member")
public class MemberController {
	String msgFlag = "";

	@Autowired
	MemberService memberService;
	
	@Autowired
	ProductService productService;

	@Autowired
	BCryptPasswordEncoder passwordEncoder;

	// --------------- 로그인 ----------------
	// 로그인 폼 호출
	@RequestMapping(value = "/memLogin")
	public String memLoginGet(HttpServletRequest request) {
		// 로그인폼 호출시 기존에 저장된 쿠키가 있으면 불러온다.
		Cookie[] cookies = request.getCookies(); // 기존에 저장된 현재 사이트 쿠키를 불러온다.(배열로)
		String mid = "";
		for (int i = 0; i < cookies.length; i++) {
			if (cookies[i].getName().equals("cMid")) {
				mid = cookies[i].getValue();
				request.setAttribute("mid", mid);
				break;
			}
		}

		return "member/memLogin";
	}
	
	// 로그인 인증처리 (아이디,비번입력)
	@RequestMapping(value="/memLogin", method = RequestMethod.POST)
	public String memLoginPost(String mid, String pwd, HttpSession session, HttpServletRequest request, HttpServletResponse response ) {
		MemberVO vo = memberService.getIdCheck(mid);
		
		//	System.out.println(passwordEncoder.encode(pwd));
		if(vo != null && passwordEncoder.matches(pwd, vo.getPwd()) && vo.getUserDel().equals("NO")) {
			int level = vo.getLevel();
			String strLevel = "";
			
			if(level == 0) strLevel = "관리자";
			else if(level == 1) strLevel = "정회원";
			else if(level == 2) strLevel = "우수회원";
			else if(level == 3) strLevel = "특별회원";
			
			session.setAttribute("sMid", mid);
			session.setAttribute("sNickName", vo.getNickName());
			session.setAttribute("sLevel", level);
			session.setAttribute("sStrLevel", strLevel);
			
			// 아이디에 대한 정보를 쿠키로 저장처리
			String idCheck = request.getParameter("idCheck")==null ? "" : request.getParameter("idCheck");
			
			// 쿠키 처리(아이디에 대한 정보를 쿠키로 저장할지를 처리한다)-jsp에서 idCheck변수에 값이 체크되어서 넘어오면 'on'값이 담겨서 넘어오게 된다.
			if(idCheck.equals("on")) {				// 앞의 jsp에서 쿠키를 저장하겠다고 넘겼을경우...
				Cookie cookie = new Cookie("cMid", mid);
				cookie.setMaxAge(60*60*24*4); 	// 쿠키의 만료시간을 4일로 정했다.(단위: 초)
				response.addCookie(cookie);
			}
			else {		// jsp에서 쿠키저장을 취소해서 보낸다면? 쿠키명을 삭제처리한다.
				Cookie[] cookies = request.getCookies();	// 기존에 저장되어 있는 현재 사이트의 쿠키를 불러와서 배열로 저장한다.
				for(int i=0; i<cookies.length; i++) {
					if(cookies[i].getName().equals("cMid")) {
						cookies[i].setMaxAge(0);		//  저장된 쿠키명중 'cMid' 쿠키를 찾아서 삭제한다.
						response.addCookie(cookies[i]);
						break;
					}
				}
			}
			
			msgFlag = "memLoginOk";
		}
		else {
			msgFlag = "memLoginNo";
		}
		
		return "redirect:/msg/" + msgFlag;
	}
	
	//로그아웃
	@RequestMapping("/memLogout")
	public String memLogoutGet(){
		
		msgFlag = "memLogout";
		
		return "redirect:/msg/" + msgFlag;
	}
	
	//로그인 성공후 만나는 회원메인창 일단 보류
//	@RequestMapping("/memMain")
//	public String memMainGet() {
//		
//		return "home";
//	}
	

	// ---------------------------- 회원가입 -------------------------
	// 회원가입 페이지
	@RequestMapping(value = "/memJoin", method = RequestMethod.GET)
	public String memInputGet() {
		return "member/memJoin";
	}

	// 아이디 중복체크 (ajax처리)
	@ResponseBody
	@RequestMapping(value = "/idCheck", method = RequestMethod.POST)
	public String idCheckPost(String mid) {
		String res = "0";
		MemberVO vo = memberService.getIdCheck(mid);
		if (vo != null)
			res = "1";

		return res;
	}

	// 닉네임 중복체크
	@ResponseBody
	@RequestMapping(value = "/nickCheck", method = RequestMethod.POST)
	public String nickNameCheckPost(String nickName) {
		String res = "0";
		MemberVO vo = memberService.getNickNameCheck(nickName);
		if (vo != null)
			res = "1";
		return res;
	}
	
	// 회원가입 처리
	@RequestMapping(value ="/memJoin", method = RequestMethod.POST)
	public String memJoinPost(MemberVO vo) {
		
		// 아이디 중복체크
		if(memberService.getIdCheck(vo.getMid()) != null) { // back단에서 중복체크하기
			msgFlag = "memIdCheckNo";
			return "redirect:/msg/ " + msgFlag;
		}
		// 닉네임 중복체크
		if(memberService.getNickNameCheck(vo.getNickName()) != null) {
			msgFlag = "memNickNameCheckNo";
			return "redirect:/msg/" + msgFlag;
		}
		
		// 비밀번호 암호화처리
		vo.setPwd(passwordEncoder.encode(vo.getPwd()));
		
		// DB에 가입회원 등록하기
		int res = memberService.setMemInput(vo);
		
		if(res == 1) msgFlag = "memInputOk";
		else msgFlag = "memInputNo";
		
		
		return "redirect:/msg/" + msgFlag;
		
	}
	
	// -----------------정보 수정--------------------------
	@RequestMapping(value="/memPwdCheck")
	public String memPwdCheckGet() {
		return "member/memPwdCheck";
	}
	
	// 비밀번호 확인
	@RequestMapping(value="/memPwdCheck", method = RequestMethod.POST)
	public String memPwdCheckPost(String pwd, HttpSession session, Model model) {
		String mid = (String)session.getAttribute("sMid");
		MemberVO vo = memberService.getIdCheck(mid);
		
		if(vo != null && passwordEncoder.matches(pwd, vo.getPwd())) {
			session.setAttribute("sPwd", pwd);
			model.addAttribute("vo",vo);
			return "redirect:/member/memUpdate";
		}
		else {
			msgFlag = "pwdCheckNo";
			return "redirect:/msg/" + msgFlag;
		}
	}
	
	// 정보수정 페이지로
	@RequestMapping(value="/memUpdate")
	public String memUpdateGet(HttpSession session, Model model) {
		String mid = (String)session.getAttribute("sMid");
		MemberVO vo = memberService.getIdCheck(mid);
		model.addAttribute("vo",vo);
		
		return "member/memUpdate";
	}
	
	// 정보수정 버튼클릭
	@RequestMapping(value="/memUpdate", method = RequestMethod.POST)
	public String memUpdatePost(MemberVO vo, HttpSession session) {
		String nickName = (String) session.getAttribute("sNickName");
		
		// 닉네임 중복체크하기 (닉네임이 변경되었으면 중복체크를 누르게 한다)
		if(!nickName.equals(vo.getNickName())) {     // 닉네임이 원래랑 다르면
		// 닉네임 중복체크
			if(memberService.getNickNameCheck(vo.getNickName()) != null) {
				msgFlag = "memNickNameCheckNo";
				return "redirect:/msg/" + msgFlag;
			}
			else {
				session.setAttribute("sNickName", vo.getNickName());
			}
		}
			
		vo.setPwd(passwordEncoder.encode(vo.getPwd()));
		
		int res = memberService.setMemUpdate(vo);
		
		if(res == 1) {
			msgFlag = "memUpdateOk";
		}
		else {
			msgFlag = "memUpdateNo";
		}
		
		return "redirect:/msg/" + msgFlag;
		
	}
	
	//------------------- 아이디, 비밀번호 찾기 -------------------------------
	//아이디 찾기 폼
	@RequestMapping(value="/idFind", method = RequestMethod.GET)
	public String idFindGet() {
		return "member/idFind";
	}
	//아이디 찾기 
	@RequestMapping(value="/idFind", method = RequestMethod.POST)
	public String idFindPost(String email, Model model) {
		
		ArrayList<MemberVO> vos = memberService.getIdFind(email);
		
		if(vos.size() != 0) {
			model.addAttribute("vos",vos);
			return "member/idSearchList";
		}
		else {
			msgFlag = "idFindNo";
			return "redirect:/msg/"+msgFlag;
		}
	}
	
	//비밀번호 찾기 폼
	@RequestMapping(value="/pwdFind", method = RequestMethod.GET)
	public String pwdFindGet() {
		return "member/pwdFind";
	}
	
	//비밀번호 찾기 폼
	@RequestMapping(value="/pwdFind", method = RequestMethod.POST)
	public String pwdFindPost(String mid, String email) {
		MemberVO vo = memberService.getPwdFind(mid,email);
		if(vo != null) { // 조건에 맞는 회ㅏ원 존재시
			// 임시비밀번호 발급
			UUID uid = UUID.randomUUID();
			String pwd = uid.toString().substring(0,10);
			memberService.setPwdChange(mid, passwordEncoder.encode(pwd));
			
			String content = pwd;
			return "redirect:/mail/pwdFindSend/"+email+"/"+content+"/";
		}
		else {
			msgFlag = "pwdFindNo";
			return "redirect:/msg/"+msgFlag; 
		}
	}
	
	// 회원 탈퇴
	@RequestMapping(value="/memDelete")
	public String memDeleteGet(HttpSession session) {
		String mid = (String) session.getAttribute("sMid");
		memberService.setMemDelete(mid);
		
		msgFlag = "memDeleteOk";
		
		return "redirect:/msg/" + msgFlag;
	}
	
	// mypage
	@RequestMapping(value="/mypage")
	public String mypageGet(HttpSession session, Model model) {
		String mid = (String) session.getAttribute("sMid");
		List<BaesongVO> bVos = productService.getBaesong(mid);
		
		model.addAttribute("vo", memberService.getIdCheck(mid));
		model.addAttribute("bVos", bVos);
		return "member/mypage/mypage";
	}

}
