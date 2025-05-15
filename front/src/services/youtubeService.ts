export const fetchYouTubeVideoInfo = async (url: string) => {
  try {
    const response = await fetch(`/api/player?url=${url}`);

    const data = await response.json();

    if (response.status !== 200) {
      throw new Error(data.message);
    }

    return data.data;
  } catch (error) {
    throw new Error(error.message);
  }
};
