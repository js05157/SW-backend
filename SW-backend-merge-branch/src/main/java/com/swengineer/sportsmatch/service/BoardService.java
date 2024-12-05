package com.swengineer.sportsmatch.service;

import com.swengineer.sportsmatch.dto.BoardDTO;
import com.swengineer.sportsmatch.entity.BoardEntity;
import com.swengineer.sportsmatch.entity.UserEntity;
import com.swengineer.sportsmatch.repository.BoardRepository;
import com.swengineer.sportsmatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    // 게시글 작성
    public BoardDTO createBoardPost(BoardDTO boardDTO, int userId, String postType) {
        Optional<UserEntity> user = userRepository.findById(userId);
        if (user.isPresent()) {
            BoardEntity boardEntity = BoardEntity.toSaveEntity(boardDTO, user.get(), postType);
            boardRepository.save(boardEntity);
            return BoardDTO.toBoardDTO(boardEntity, userId);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id " + userId + " not found");
        }
    }

    // 게시글 목록 조회
    public List<BoardDTO> getBoardPosts(String postType) {
        List<BoardEntity> boardEntities = boardRepository.findByPost_type(postType);
        return boardEntities.stream()
                .map(boardEntity -> BoardDTO.toBoardDTO(boardEntity, boardEntity.getUser_entity().getUserId()))
                .toList();
    }

    // 게시글 상세 조회
    public BoardDTO getBoardPost(int postId) {
        Optional<BoardEntity> boardEntityOptional = boardRepository.findById(postId);
        if (boardEntityOptional.isPresent()) {
            BoardEntity boardEntity = boardEntityOptional.get();
            boardEntity.setPost_hits(boardEntity.getPost_hits() + 1);
            boardRepository.save(boardEntity);
            return BoardDTO.toBoardDTO(boardEntity, boardEntity.getUser_entity().getUserId());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Board post with id " + postId + " not found");
        }
    }

    // 게시글 수정
    public BoardDTO updateBoardPost(int postId, BoardDTO boardDTO, int userId) {
        Optional<BoardEntity> boardEntityOpt = boardRepository.findById(postId);
        if (boardEntityOpt.isPresent()) {
            BoardEntity boardEntity = boardEntityOpt.get();
            if (boardEntity.getUser_entity().getUserId() != userId) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
            }
            boardEntity.setPost_title(boardDTO.getPost_title());
            boardEntity.setPost_content(boardDTO.getPost_content());
            boardRepository.save(boardEntity);
            return BoardDTO.toBoardDTO(boardEntity, boardEntity.getUser_entity().getUserId());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
    }

    // 게시글 삭제
    public void deleteBoardPost(int postId, int userId) {
        Optional<BoardEntity> boardEntityOpt = boardRepository.findById(postId);
        if (boardEntityOpt.isPresent()) {
            BoardEntity boardEntity = boardEntityOpt.get();
            if (boardEntity.getUser_entity().getUserId() != userId) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
            }
            boardRepository.deleteById(postId);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
    }

    // 특정 사용자의 게시글 목록 조회
    public List<BoardDTO> getUserBoards(int userId) {
        List<BoardEntity> boardEntities = boardRepository.findByUserEntityUserId(userId);  // 사용자의 모든 게시글 조회
        return boardEntities.stream()
                .map(boardEntity -> BoardDTO.toBoardDTO(boardEntity, boardEntity.getUser_entity().getUserId()))
                .collect(Collectors.toList());  // BoardEntity -> BoardDTO 변환
    }
}