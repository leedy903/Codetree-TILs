import java.io.*;
import java.util.*;

class Point {
    int y;
    int x;

    public Point(int y, int x) {
        this.y = y;
        this.x = x;
    }

    @Override
    public String toString() {
        return String.format("{%d, %d} ", y, x);
    }
}

public class Main {
    static int N, M;
    static Point home, park;
    static int[][] warriorMap;
    static int[][] roadMap;
    static Deque<Point> shortestPath;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = null;

        st = new StringTokenizer(br.readLine());

        int[] dy = {-1, 1, 0, 0};
        int[] dx = {0, 0, -1, 1};

        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());

        warriorMap = new int[N][N];
        roadMap = new int[N][N];

        st = new StringTokenizer(br.readLine());
        int hy = Integer.parseInt(st.nextToken());
        int hx = Integer.parseInt(st.nextToken());
        home = new Point(hy, hx);

        int py = Integer.parseInt(st.nextToken());
        int px = Integer.parseInt(st.nextToken());
        park = new Point(py, px);

        st = new StringTokenizer(br.readLine());
        for (int i = 0; i < M; i++) {
            int wy = Integer.parseInt(st.nextToken());
            int wx = Integer.parseInt(st.nextToken());
            warriorMap[wy][wx] += 1;
        }

        for (int i = 0; i < N; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < N; j++) {
                roadMap[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        // make additional road for home and park
        roadMap[home.y][home.x] = 0;
        roadMap[park.y][park.x] = 0;

        // 0. get medusa shortestPath
        shortestPath = getShortestPath();

        if (shortestPath.isEmpty()) {
            System.out.println(-1);
            br.close();
            return;
        }

        // remove park position from deq
        shortestPath.poll();

        while (!shortestPath.isEmpty()) {
            int movingDistance = 0;
            int stoneCount = 0;
            int attackCount = 0;

            // 1. medusa move
            Point medusa = shortestPath.pollLast();
            warriorMap[medusa.y][medusa.x] = 0;

            // 2. medusa sight
            int stonesIdx = 0;
            int maxSize = 0;
            boolean [][] visibleMatrix = new boolean[N][N];
            List<Point>[] stones = new ArrayList[4];
            stones[0] = getNorthStones(medusa);
            stones[1] = getSouthStones(medusa);
            stones[2] = getWestStones(medusa);
            stones[3] = getEastStones(medusa);

            for (int i = 0; i < 4; i++) {
                if (maxSize < stones[i].size()) {
                    maxSize = stones[i].size();
                    stonesIdx = i;
                }
            }

            for (Point stone : stones[stonesIdx]) {
                stoneCount += warriorMap[stone.y][stone.x];
            }

            if (stonesIdx == 0) visibleMatrix = getNorthVisibleMatrix(medusa);
            else if (stonesIdx == 1) visibleMatrix = getSouthVisibleMatrix(medusa);
            else if (stonesIdx == 2) visibleMatrix = getWestVisibleMatrix(medusa);
            else if (stonesIdx == 3) visibleMatrix = getEastVisibleMatrix(medusa);

            // 3. warriors move
            for (int i = 0; i < 2; i++) {
                int[][] newWarriorMap = new int[N][N];
                for (int y = 0; y < N; y++) {
                    for (int x = 0; x < N; x++) {
                        if (warriorMap[y][x] != 0) {
                            newWarriorMap[y][x] += warriorMap[y][x];
                            if (!visibleMatrix[y][x]) {
                                for (int d = 0; d < 4; d++) {
                                    int ny = y + dy[(d + (2 * i)) % 4];
                                    int nx = x + dx[(d + (2 * i)) % 4];
                                    if (0 <= ny && ny < N && 0 <= nx && nx < N) {
                                        if (getDistance(ny, nx, medusa.y, medusa.x) < getDistance(y, x, medusa.y, medusa.x) && !visibleMatrix[ny][nx]) {
                                            movingDistance += warriorMap[y][x];
                                            newWarriorMap[y][x] -= warriorMap[y][x];
                                            newWarriorMap[ny][nx] += warriorMap[y][x];
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 4. warriors attack
                attackCount += newWarriorMap[medusa.y][medusa.x];
                newWarriorMap[medusa.y][medusa.x] = 0;
                for (int y = 0; y < N; y++) {
                    for (int x = 0; x < N; x++) {
                        warriorMap[y][x] = newWarriorMap[y][x];
                    }
                }
            }

            sb.append(movingDistance).append(" ").append(stoneCount).append(" ").append(attackCount).append("\n");
        }
        sb.append(0);

        System.out.println(sb);

        br.close();
    }

    public static Deque<Point> getShortestPath() {
        Deque<Point> shortestPath = new ArrayDeque<>();
        int [][] pathMatrix = getPathMatrix();
        if (pathMatrix[park.y][park.x] == -1) return shortestPath;

        int idx = park.y * N + park.x + 1;

        while (true) {
            if (idx == home.y * N + home.x + 1) break;
            int y = (idx - 1) / N;
            int x = (idx - 1) % N;
            shortestPath.offer(new Point(y , x));
            idx = pathMatrix[y][x];
        }

        return shortestPath;
    }

    public static int getDistance(int y1, int x1, int y2, int x2) {
        return Math.abs(y1 - y2) + Math.abs(x1 - x2);
    }

    public static int[][] getPathMatrix() {
        int[] dy = {-1, 1, 0, 0};
        int[] dx = {0, 0, -1, 1};

        boolean [][] visited = new boolean[N][N];
        int [][] pathMatrix = new int[N][N];

        for (int i = 0; i < N; i++) {
            Arrays.fill(pathMatrix[i], -1);
        }

        Deque<Point> deq = new ArrayDeque<>();
        deq.offer(new Point(home.y, home.x));
        visited[home.y][home.x] = true;
        while (!deq.isEmpty()) {
            Point cur = deq.poll();
            for (int d = 0; d < 4; d++) {
                int ny = cur.y + dy[d];
                int nx = cur.x + dx[d];
                if (0 <= ny && ny < N && 0 <= nx && nx < N) {
                    if (roadMap[ny][nx] == 0 && !visited[ny][nx]) {
                        visited[ny][nx] = true;
                        pathMatrix[ny][nx] = cur.y * N + cur.x + 1;
                        deq.offer(new Point(ny, nx));
                    }
                }
            }
        }
        return pathMatrix;
    }

    public static List<Point> getNorthStones(Point medusa) {
        List<Point> stones = new ArrayList<>();
        int start, end;
        start = medusa.y - 1;
        end = -1;
        for (int x = medusa.x - 1; x > -1; x--) {
            for (int y = start; y > end; y--) {
                if (warriorMap[y][x] != 0) {
                    stones.add(new Point(y, x));
                    end = y;
                }
            }
            start--;
            end = Math.max(-1, end - 1);
        }

        start = medusa.y - 1;
        end = -1;
        for (int y = start; y > end; y--) {
            if (warriorMap[y][medusa.x] != 0) {
                stones.add(new Point(y, medusa.x));
                break;
            }
        }

        start = medusa.y - 1;
        end = -1;
        for (int x = medusa.x + 1; x < N; x++) {
            for (int y = start; y > end; y--) {
                if (warriorMap[y][x] != 0) {
                    stones.add(new Point(y, x));
                    end = y;
                }
            }
            start--;
            end = Math.max(-1, end - 1);
        }
        return stones;
    }

    public static List<Point> getSouthStones(Point medusa) {
        List<Point> stones = new ArrayList<>();
        int start, end;

        start = medusa.y + 1;
        end = N;
        for (int x = medusa.x - 1; x > -1; x--) {
            for (int y = start; y < end; y++) {
                if (warriorMap[y][x] != 0) {
                    stones.add(new Point(y, x));
                    end = y;
                }
            }
            start++;
            end = Math.min(N, end + 1);
        }

        start = medusa.y + 1;
        end = N;
        for (int y = start; y < end; y++) {
            if (warriorMap[y][medusa.x] != 0) {
                stones.add(new Point(y, medusa.x));
                break;
            }
        }

        start = medusa.y + 1;
        end = N;
        for (int x = medusa.x + 1; x < N; x++) {
            for (int y = start; y < end; y++) {
                if (warriorMap[y][x] != 0) {
                    stones.add(new Point(y, x));
                    end = y;
                }
            }
            start++;
            end = Math.min(N, end + 1);
        }
        return stones;
    }

    public static List<Point> getWestStones(Point medusa) {
        List<Point> stones = new ArrayList<>();
        int start, end;

        start = medusa.x - 1;
        end = -1;
        for (int y = medusa.y - 1; y > -1; y--) {
            for (int x = start; x > end; x--) {
                if (warriorMap[y][x] != 0) {
                    stones.add(new Point(y, x));
                    end = x;
                }
            }
            start--;
            end = Math.max(-1, end - 1);
        }

        start = medusa.x - 1;
        end = -1;
        for (int x = start; x > end; x--) {
            if (warriorMap[medusa.y][x] != 0) {
                stones.add(new Point(medusa.y, x));
                break;
            }
        }

        start = medusa.x - 1;
        end = -1;
        for (int y = medusa.y + 1; y < N; y++) {
            for (int x = start; x > end; x--) {
                if (warriorMap[y][x] != 0) {
                    stones.add(new Point(y, x));
                    end = x;
                }
            }
            start--;
            end = Math.max(-1, end - 1);
        }
        return stones;
    }

    public static List<Point> getEastStones(Point medusa) {
        List<Point> stones = new ArrayList<>();
        int start, end;

        start = medusa.x + 1;
        end = N;
        for (int y = medusa.y - 1; y > -1; y--) {
            for (int x = start; x < end; x++) {
                if (warriorMap[y][x] != 0) {
                    stones.add(new Point(y, x));
                    end = x;
                }
            }
            start++;
            end = Math.min(N, end + 1);
        }

        start = medusa.x + 1;
        end = N;
        for (int x = start; x < end; x++) {
            if (warriorMap[medusa.y][x] != 0) {
                stones.add(new Point(medusa.y, x));
                break;
            }
        }

        start = medusa.x + 1;
        end = N;
        for (int y = medusa.y + 1; y < N; y++) {
            for (int x = start; x < end; x++) {
                if (warriorMap[y][x] != 0) {
                    stones.add(new Point(y, x));
                    end = x;
                }
            }
            start++;
            end = Math.min(N, end + 1);
        }
        return stones;
    }

    public static boolean[][] getNorthVisibleMatrix(Point medusa) {
        boolean[][] visibleMatrix = new boolean[N][N];
        int start, end;
        start = medusa.y - 1;
        end = -1;
        for (int x = medusa.x - 1; x > -1; x--) {
            for (int y = start; y > end; y--) {
                visibleMatrix[y][x] = true;
                if (warriorMap[y][x] != 0) {
                    end = y;
                }
            }
            start--;
            end = Math.max(-1, end - 1);
        }

        start = medusa.y - 1;
        end = -1;
        for (int y = start; y > end; y--) {
            visibleMatrix[y][medusa.x] = true;
            if (warriorMap[y][medusa.x] != 0) {
                break;
            }
        }

        start = medusa.y - 1;
        end = -1;
        for (int x = medusa.x + 1; x < N; x++) {
            for (int y = start; y > end; y--) {
                visibleMatrix[y][x] = true;
                if (warriorMap[y][x] != 0) {
                    end = y;
                }
            }
            start--;
            end = Math.max(-1, end - 1);
        }
        return visibleMatrix;
    }

    public static boolean[][] getSouthVisibleMatrix(Point medusa) {
        boolean[][] visibleMatrix = new boolean[N][N];
        int start, end;

        start = medusa.y + 1;
        end = N;
        for (int x = medusa.x - 1; x > -1; x--) {
            for (int y = start; y < end; y++) {
                visibleMatrix[y][x] = true;
                if (warriorMap[y][x] != 0) {
                    end = y;
                }
            }
            start++;
            end = Math.min(N, end + 1);
        }

        start = medusa.y + 1;
        end = N;
        for (int y = start; y < end; y++) {
            visibleMatrix[y][medusa.x] = true;
            if (warriorMap[y][medusa.x] != 0) {
                break;
            }
        }

        start = medusa.y + 1;
        end = N;
        for (int x = medusa.x + 1; x < N; x++) {
            for (int y = start; y < end; y++) {
                visibleMatrix[y][x] = true;
                if (warriorMap[y][x] != 0) {
                    end = y;
                }
            }
            start++;
            end = Math.min(N, end + 1);
        }
        return visibleMatrix;
    }

    public static boolean[][] getWestVisibleMatrix(Point medusa) {
        boolean[][] visibleMatrix = new boolean[N][N];
        int start, end;

        start = medusa.x - 1;
        end = -1;
        for (int y = medusa.y - 1; y > -1; y--) {
            for (int x = start; x > end; x--) {
                visibleMatrix[y][x] = true;
                if (warriorMap[y][x] != 0) {
                    end = x;
                }
            }
            start--;
            end = Math.max(-1, end - 1);
        }

        start = medusa.x - 1;
        end = -1;
        for (int x = start; x > end; x--) {
            visibleMatrix[medusa.y][x] = true;
            if (warriorMap[medusa.y][x] != 0) {
                break;
            }
        }

        start = medusa.x - 1;
        end = -1;
        for (int y = medusa.y + 1; y < N; y++) {
            for (int x = start; x > end; x--) {
                visibleMatrix[y][x] = true;
                if (warriorMap[y][x] != 0) {
                    end = x;
                }
            }
            start--;
            end = Math.max(-1, end - 1);
        }
        return visibleMatrix;
    }

    public static boolean[][] getEastVisibleMatrix(Point medusa) {
        boolean[][] visibleMatrix = new boolean[N][N];
        int start, end;

        start = medusa.x + 1;
        end = N;
        for (int y = medusa.y - 1; y > -1; y--) {
            for (int x = start; x < end; x++) {
                visibleMatrix[y][x] = true;
                if (warriorMap[y][x] != 0) {
                    end = x;
                }
            }
            start++;
            end = Math.min(N, end + 1);
        }

        start = medusa.x + 1;
        end = N;
        for (int x = start; x < end; x++) {
            visibleMatrix[medusa.y][x] = true;
            if (warriorMap[medusa.y][x] != 0) {
                break;
            }
        }

        start = medusa.x + 1;
        end = N;
        for (int y = medusa.y + 1; y < N; y++) {
            for (int x = start; x < end; x++) {
                visibleMatrix[y][x] = true;
                if (warriorMap[y][x] != 0) {
                    end = x;
                }
            }
            start++;
            end = Math.min(N, end + 1);
        }
        return visibleMatrix;
    }
}
