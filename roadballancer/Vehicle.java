import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;
/**
 * Write a description of class Vehicle here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Vehicle extends Actor
{
    private int current_row;
    private int current_col;
    private int target_row;
    private int target_col;
    private List<int[]> path;
    /**
     * Act - do whatever the Vehicle wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    public void act()
    {
        System.out.println("path: " + path);
        if (path != null && !path.isEmpty()) {
            int[] next = path.get(0);
            int next_row = next[0];
            int next_col = next[1];
            setLocation(next_col * 32 + 16, next_row * 32 + 16);
            path.remove(0);
        }
    }
    public void addedToWorld(World world) {
        path = findPath(current_row, current_col, target_row, target_col);
        System.out.println("path calculated: " + path);
    }       
    public Vehicle(int start_row, int start_col, int target_row, int target_col){
        this.current_row = start_row;
        this.current_col = start_col;
        this.target_row = target_row;
        this.target_col = target_col;
    }
    private List<int[]> findPath(int start_row, int start_col, int target_row, int target_col){
        int[][] map = Level.map;
        System.out.println("start tile: " + map[start_row][start_col]);
        System.out.println("target tile: " + map[target_row][target_col]);
        System.out.println("start: " + start_row + "," + start_col);
        System.out.println("target: " + target_row + "," + target_col);
        boolean[][] visited = new boolean[20][20];
        int[][] came_from_row = new int[20][20];
        int[][] came_from_col = new int[20][20];
        
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{start_row, start_col});
        visited[start_row][start_col] = true;
        
        int[] dr = {-1, 1, 0, 0};  // up, down, left, right
        int[] dc = {0, 0, -1, 1};
        
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0];
            int c = current[1];
            
            if (r == target_row && c == target_col) {
                return tracePath(came_from_row, came_from_col, start_row, start_col, target_row, target_col);
            }
            
            for (int i = 0; i < 4; i++) {
                int nr = r + dr[i];
                int nc = c + dc[i];
                if (nr >= 0 && nr < 20 && nc >= 0 && nc < 20 && !visited[nr][nc] && map[nr][nc] != 0) {
                    visited[nr][nc] = true;
                    came_from_row[nr][nc] = r;
                    came_from_col[nr][nc] = c;
                    queue.add(new int[]{nr, nc});
                }
            }
        }
        return null; // no path found
    }
    private List<int[]> tracePath(int[][] came_from_row, int[][] came_from_col, int start_row, int start_col, int target_row, int target_col){
        List<int[]> path = new ArrayList<>();
        int r = target_row;
        int c = target_col;
        while (r != start_row || c != start_col) {
            path.add(new int[]{r, c});
            int prev_r = came_from_row[r][c];
            int prev_c = came_from_col[r][c];
            r = prev_r;
            c = prev_c;
        }
        path.add(new int[]{start_row, start_col});
        Collections.reverse(path);
        return path;
    }
}

